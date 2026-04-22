package it.polimi.ingsw.am23.controller;

import it.polimi.ingsw.am23.jsonParsing.Parser;
import it.polimi.ingsw.am23.model.ActionResult;
import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.ModelObserver;
import it.polimi.ingsw.am23.model.cards.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.model.cards.SelectedCards;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.setup.PlayerConnectionInfo;
import it.polimi.ingsw.am23.model.setup.Setup;
import it.polimi.ingsw.am23.model.state.GameState;
import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.VirtualServer;
import it.polimi.ingsw.am23.network.VirtualView;

import java.util.*;

/**
 * Controller MVC
 * Gestisce lobby, partita e bootstrap modello
 */

public final class GameController implements VirtualServer, ModelObserver {

    private static final int DEFAULT_LOBBY_MAX_PLAYERS = 5;
    private static final int LOBBY_CODE_LENGTH = 4;
    private static final String LOBBY_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final Map<String, VirtualView> clientsByPlayerId = new HashMap<>();
    private final Map<String, PlayerConnectionInfo> playersById = new HashMap<>();
    private final Map<String, String> lobbyByPlayerId = new HashMap<>();
    private final Map<String, LobbyRoom> lobbiesById = new LinkedHashMap<>();
    private final Map<String, Game> gamesByLobbyId = new HashMap<>();
    private final Random random = new Random();
    private String activeLobbyId;

    public synchronized void connect(String playerName, VirtualView client) throws Exception {
        Objects.requireNonNull(playerName, "playerName cannot be null");
        Objects.requireNonNull(client, "client cannot be null");

        String normalizedName = playerName.trim();
        if (normalizedName.isEmpty()) {
            client.onConnectError("Player name cannot be empty.");
            return;
        }

        boolean nicknameAlreadyUsed = playersById.values().stream()
                .anyMatch(p -> p.getNickname().equalsIgnoreCase(normalizedName));
        if (nicknameAlreadyUsed) {
            client.onConnectError("Player name already in use.");
            return;
        }

        String playerId = UUID.randomUUID().toString();
        PlayerConnectionInfo info = new PlayerConnectionInfo(playerId, normalizedName);
        playersById.put(playerId, info);
        clientsByPlayerId.put(playerId, client);

        client.onConnected(playerId, currentLobbyStates());
        broadcastLobbyList();
    }

    public synchronized void createLobby(String playerId, String lobbyName, int maxPlayers) throws Exception {
        PlayerConnectionInfo owner = requireConnectedPlayer(playerId);
        String normalizedLobbyName = lobbyName == null ? "" : lobbyName.trim();
        if (normalizedLobbyName.isEmpty()) {
            throw new IllegalArgumentException("Lobby name cannot be empty.");
        }
        String lobbyId = generateLobbyId();
        int capacity = maxPlayers > 0 ? maxPlayers : DEFAULT_LOBBY_MAX_PLAYERS;

        LobbyState lobbyState = new LobbyState(lobbyId, normalizedLobbyName, playerId, capacity);
        lobbyState.addPlayer(owner);

        LobbyRoom lobbyRoom = new LobbyRoom(lobbyState);
        lobbiesById.put(lobbyId, lobbyRoom);
        lobbyByPlayerId.put(playerId, lobbyId);

        clientsByPlayerId.get(playerId).onLobbyCreated(copyLobby(lobbyState));
        broadcastLobbyUpdate(lobbyRoom);
        broadcastLobbyList();
    }

    public synchronized void joinLobby(String playerId, String lobbyId) throws Exception {
        PlayerConnectionInfo player = requireConnectedPlayer(playerId);
        LobbyRoom lobby = requireLobby(lobbyId);

        if (lobby.state.isFull()) {
            clientsByPlayerId.get(playerId).onJoinError("Lobby is full.");
            return;
        }

        boolean alreadyInside = lobby.state.getPlayers().stream()
                .anyMatch(p -> p.getId().equals(playerId));
        if (alreadyInside) {
            return;
        }

        lobby.state.addPlayer(player);
        lobbyByPlayerId.put(playerId, lobbyId);
        broadcastLobbyUpdate(lobby);
        broadcastLobbyList();
    }

    public synchronized void leaveLobby(String playerId, String lobbyId) throws Exception {
        requireConnectedPlayer(playerId);
        LobbyRoom lobby = requireLobby(lobbyId);

        lobby.state.removePlayer(playerId);
        lobbyByPlayerId.remove(playerId);

        if (lobby.state.getOwnerPlayerId().equals(playerId) || lobby.state.getCurrentPlayers() == 0) {
            lobbiesById.remove(lobbyId);
            for (String memberId : lobby.memberIds()) {
                lobbyByPlayerId.remove(memberId);
                VirtualView view = clientsByPlayerId.get(memberId);
                if (view != null) {
                    view.onLobbyClosed();
                }
            }
            broadcastLobbyList();
            return;
        }

        broadcastLobbyUpdate(lobby);
        broadcastLobbyList();
    }

    public synchronized void startGame(String playerId, String lobbyId) throws Exception {
        requireConnectedPlayer(playerId);
        LobbyRoom lobby = requireLobby(lobbyId);

        if (gamesByLobbyId.containsKey(lobbyId)) {
            throw new IllegalStateException("Game already started for lobby: " + lobbyId);
        }

        if (!lobby.state.getOwnerPlayerId().equals(playerId)) {
            throw new IllegalStateException("Only lobby owner can start the game.");
        }
        if (lobby.state.getCurrentPlayers() < 2) {
            throw new IllegalStateException("At least 2 players are required to start the game.");
        }

        List<PlayerConnectionInfo> players = new ArrayList<>(lobby.state.getPlayers());
        Setup setup = new Parser().parse(players);
        Game game = setup.make();
        gamesByLobbyId.put(lobbyId, game);
        game.addObserver(this);

        withActiveLobby(lobbyId, () -> {
            game.startGame();
            return ActionResult.success(it.polimi.ingsw.am23.model.enums.ActionType.GENERIC, "Game started");
        });
    }

    @Override
    public synchronized void placeTotem(String playerId, char offerTileChar) throws Exception {
        String lobbyId = requireLobbyIdForPlayer(playerId);
        Game game = requireGame(lobbyId);
        ActionResult result = withActiveLobby(lobbyId, () -> game.placeTotem(playerId, offerTileChar));
        handleActionResult(playerId, result);
    }

    @Override
    public synchronized void takeCards(String playerId, SelectedCards selectedCards) throws Exception {
        String lobbyId = requireLobbyIdForPlayer(playerId);
        Game game = requireGame(lobbyId);
        ActionResult result = withActiveLobby(lobbyId, () -> game.takeCards(playerId, selectedCards));
        handleActionResult(playerId, result);
        if (result.isSuccess()) {
            broadcastEndOfDrawingPhase(lobbyId, game.getGameState());
            advanceGameFlow(lobbyId);
        }
    }

    @Override
    public synchronized void takeExtraCard(String playerId, int index) throws Exception {
        String lobbyId = requireLobbyIdForPlayer(playerId);
        Game game = requireGame(lobbyId);
        SelectedCardExtraDraw selectedCardExtraDraw = new SelectedCardExtraDraw(index, null);
        ActionResult result = withActiveLobby(lobbyId, () -> game.takeExtraCard(playerId, selectedCardExtraDraw));
        handleActionResult(playerId, result);
        if (result.isSuccess()) {
            broadcastEndOfDrawingPhase(lobbyId, game.getGameState());
            advanceGameFlow(lobbyId);
        }
    }

    @Override
    public synchronized void onGameStarted(GameState gameState) {
        broadcastGameStarted(activeLobbyId, gameState);
    }

    @Override
    public synchronized void onGameStateChanged(GameState gameState) {
        broadcastGameState(activeLobbyId);
    }

    @Override
    public synchronized void onEndOfPlacingPhase(GameState gameState) {
        broadcastEndOfPlacingPhase(activeLobbyId, gameState);
    }

    @Override
    public synchronized void onEndOfDrawingPhase(GameState gameState) {
        broadcastEndOfDrawingPhase(activeLobbyId, gameState);
    }

    @Override
    public synchronized void onExtraDrawRequest(GameState gameState) {
        broadcastExtraDrawRequest(activeLobbyId, gameState);
    }

    @Override
    public synchronized void onEventResolved(GameState gameState) {
        broadcastGameState(activeLobbyId);
    }

    @Override
    public synchronized void onEraProgression(GameState gameState) {
        broadcastEraProgression(activeLobbyId, gameState);
    }

    @Override
    public synchronized void onGameOver(GameState gameState) {
        broadcastGameOver(activeLobbyId, gameState);
    }

    @Override
    public synchronized void onScoreboardAvailable(GameState gameState) {
        broadcastScoreboardAvailable(activeLobbyId, gameState);
    }

    private ActionResult withActiveLobby(String lobbyId, GameAction action) throws Exception {
        String previousLobbyId = activeLobbyId;
        activeLobbyId = lobbyId;
        try {
            return action.run();
        } finally {
            activeLobbyId = previousLobbyId;
        }
    }

    private void handleActionResult(String playerId, ActionResult result) throws Exception {
        if (result == null || result.isSuccess()) {
            return;
        }
        VirtualView view = clientsByPlayerId.get(playerId);
        if (view != null) {
            view.onActionError(result.getActionType(), result.getMessage());
        }
    }

    private void advanceGameFlow(String lobbyId) throws Exception {
        Game game = gamesByLobbyId.get(lobbyId);
        if (game == null) {
            return;
        }

        while (true) {
            GamePhase phase = game.getGamePhase();
            if (phase == GamePhase.RESOLVING_EVENTS) {
                withActiveLobby(lobbyId, game::resolveEvents);
                if (game.getGamePhase() == GamePhase.ENDED) {
                    withActiveLobby(lobbyId, game::calculateScores);
                } else {
                    broadcastEndOfResolvingPhase(lobbyId, game.getGameState());
                }
                continue;
            }
            if (phase == GamePhase.ENDED) {
                withActiveLobby(lobbyId, game::calculateScores);
            }
            break;
        }
    }

    private String requireLobbyIdForPlayer(String playerId) {
        String lobbyId = lobbyByPlayerId.get(playerId);
        if (lobbyId == null) {
            throw new IllegalArgumentException("Player is not inside a lobby: " + playerId);
        }
        return lobbyId;
    }

    private Game requireGame(String lobbyId) {
        Game game = gamesByLobbyId.get(lobbyId);
        if (game == null) {
            throw new IllegalStateException("Game not started for lobby: " + lobbyId);
        }
        return game;
    }

    private void broadcastGameStarted(String lobbyId, it.polimi.ingsw.am23.model.state.GameState gameState) {
        broadcastToLobby(lobbyId, view -> view.onGameStarted(gameState));
    }

    private void broadcastGameState(String lobbyId) {
        Game game = gamesByLobbyId.get(lobbyId);
        if (game != null) {
            broadcastToLobby(lobbyId, view -> view.onGameStateChanged(game.getGameState()));
        }
    }

    private void broadcastEndOfPlacingPhase(String lobbyId, it.polimi.ingsw.am23.model.state.GameState gameState) {
        broadcastToLobby(lobbyId, view -> view.onEndOfPlacingPhase(gameState));
    }

    private void broadcastEndOfDrawingPhase(String lobbyId, it.polimi.ingsw.am23.model.state.GameState gameState) {
        broadcastToLobby(lobbyId, view -> view.onEndOfDrawingPhase(gameState));
    }

    private void broadcastExtraDrawRequest(String lobbyId, it.polimi.ingsw.am23.model.state.GameState gameState) {
        broadcastToLobby(lobbyId, view -> view.onExtraDrawRequest(gameState));
    }

    private void broadcastEndOfResolvingPhase(String lobbyId, it.polimi.ingsw.am23.model.state.GameState gameState) {
        broadcastToLobby(lobbyId, view -> view.onEndOfResolvingPhase(gameState));
    }

    private void broadcastEraProgression(String lobbyId, it.polimi.ingsw.am23.model.state.GameState gameState) {
        broadcastToLobby(lobbyId, view -> view.onEraProgression(gameState));
    }

    private void broadcastGameOver(String lobbyId, GameState gameState) {
        broadcastToLobby(lobbyId, view -> view.onGameOver(gameState));
    }

    private void broadcastScoreboardAvailable(String lobbyId, GameState gameState) {
        broadcastToLobby(lobbyId, view -> view.onScoreboardAvailable(gameState));
    }

    private void broadcastToLobby(String lobbyId, RemoteViewAction action) {
        if (lobbyId == null) {
            return;
        }
        LobbyRoom lobby = lobbiesById.get(lobbyId);
        if (lobby == null) {
            return;
        }
        for (String memberId : lobby.memberIds()) {
            VirtualView view = clientsByPlayerId.get(memberId);
            if (view != null) {
                try {
                    action.apply(view);
                } catch (Exception ignored) {
                    // Ignore transient remote failures during broadcast.
                }
            }
        }
    }

    private PlayerConnectionInfo requireConnectedPlayer(String playerId) {
        PlayerConnectionInfo info = playersById.get(playerId);
        if (info == null) {
            throw new IllegalArgumentException("Player is not connected: " + playerId);
        }
        return info;
    }

    private LobbyRoom requireLobby(String lobbyId) {
        LobbyRoom lobby = lobbiesById.get(lobbyId);
        if (lobby == null) {
            throw new IllegalArgumentException("Lobby not found: " + lobbyId);
        }
        return lobby;
    }

    private void broadcastLobbyList() throws Exception {
        List<LobbyState> lobbyStates = currentLobbyStates();
        for (VirtualView view : clientsByPlayerId.values()) {
            view.onLobbyListUpdated(lobbyStates);
        }
    }

    private List<LobbyState> currentLobbyStates() {
        return lobbiesById.values().stream()
                .map(lobby -> copyLobby(lobby.state))
                .toList();
    }

    private void broadcastLobbyUpdate(LobbyRoom lobby) throws Exception {
        LobbyState snapshot = copyLobby(lobby.state);
        for (String memberId : lobby.memberIds()) {
            VirtualView view = clientsByPlayerId.get(memberId);
            if (view != null) {
                view.onLobbyUpdate(snapshot);
            }
        }
    }

    private LobbyState copyLobby(LobbyState source) {
        LobbyState copy = new LobbyState(
                source.getLobbyId(),
                source.getLobbyName(),
                source.getOwnerPlayerId(),
                source.getMaxPlayers()
        );
        for (PlayerConnectionInfo p : source.getPlayers()) {
            copy.addPlayer(p);
        }
        return copy;
    }

    private String generateLobbyId() {
        for (int attempts = 0; attempts < 1000; attempts++) {
            StringBuilder builder = new StringBuilder(LOBBY_CODE_LENGTH);
            for (int i = 0; i < LOBBY_CODE_LENGTH; i++) {
                int index = random.nextInt(LOBBY_CODE_CHARS.length());
                builder.append(LOBBY_CODE_CHARS.charAt(index));
            }
            String lobbyId = builder.toString();
            if (!lobbiesById.containsKey(lobbyId)) {
                return lobbyId;
            }
        }
        throw new IllegalStateException("Unable to generate a unique lobby code.");
    }


    private record LobbyRoom(LobbyState state) {
        private List<String> memberIds() {
            return state.getPlayers().stream().map(PlayerConnectionInfo::getId).toList();
        }
    }

    @FunctionalInterface
    private interface GameAction {
        ActionResult run() throws Exception;
    }

    @FunctionalInterface
    private interface RemoteViewAction {
        void apply(VirtualView view) throws Exception;
    }
}