package it.polimi.ingsw.am23.controller;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.SelectedCards;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderTile;
import it.polimi.ingsw.am23.model.setup.PlayerConnectionInfo;
import it.polimi.ingsw.am23.model.setup.Setup;
import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.VirtualServer;
import it.polimi.ingsw.am23.network.VirtualView;
import it.polimi.ingsw.am23.setup.service.ResourceSetupFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * Controller MVC
 * Gestisce lobby, partita e bootstrap modello
 */
public final class GameController implements VirtualServer {

    private static final int DEFAULT_LOBBY_MAX_PLAYERS = 5;
    private static final int LOBBY_CODE_LENGTH = 4;
    private static final String LOBBY_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final ResourceSetupFactory setupFactory;
    private final Map<String, VirtualView> clientsByPlayerId = new HashMap<>();
    private final Map<String, PlayerConnectionInfo> playersById = new HashMap<>();
    private final Map<String, LobbyRoom> lobbiesById = new LinkedHashMap<>();
    private final Map<String, Game> gamesByLobbyId = new HashMap<>();
    private final Random random = new Random();

    public GameController() {
        this(new ResourceSetupFactory());
    }

    public GameController(ResourceSetupFactory setupFactory) {
        this.setupFactory = Objects.requireNonNull(setupFactory, "setupFactory cannot be null");
    }

    @Override
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

    @Override
    public synchronized void createLobby(String playerId, String lobbyName, int maxPlayers) throws Exception {
        PlayerConnectionInfo owner = requireConnectedPlayer(playerId);
        String normalizedLobbyName = lobbyName == null ? "" : lobbyName.trim();
        if (normalizedLobbyName.isEmpty()) {
            throw new IllegalArgumentException("Lobby name cannot be empty.");
        }
        String lobbyId = generateLobbyId();

        LobbyState lobbyState = new LobbyState(lobbyId, normalizedLobbyName, playerId, DEFAULT_LOBBY_MAX_PLAYERS);
        lobbyState.addPlayer(owner);

        LobbyRoom lobbyRoom = new LobbyRoom(lobbyState);
        lobbiesById.put(lobbyId, lobbyRoom);

        clientsByPlayerId.get(playerId).onLobbyCreated(copyLobby(lobbyState));
        broadcastLobbyUpdate(lobbyRoom);
        broadcastLobbyList();
    }

    @Override
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
        broadcastLobbyUpdate(lobby);
        broadcastLobbyList();
    }

    @Override
    public synchronized void leaveLobby(String playerId, String lobbyId) throws Exception {
        requireConnectedPlayer(playerId);
        LobbyRoom lobby = requireLobby(lobbyId);

        lobby.state.removePlayer(playerId);

        if (lobby.state.getOwnerPlayerId().equals(playerId) || lobby.state.getCurrentPlayers() == 0) {
            lobbiesById.remove(lobbyId);
            for (String memberId : lobby.memberIds()) {
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

    @Override
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
        Setup setup = setupFactory.createSetup(players, defaultTurnOrderTiles());
        Game game = setup.make();
        game.startGame();
        gamesByLobbyId.put(lobbyId, game);

        for (String memberId : lobby.memberIds()) {
            VirtualView view = clientsByPlayerId.get(memberId);
            if (view != null) {
                view.onGameStarted(game.getGameState());
            }
        }
    }

    @Override
    public void placeTotem(String playerId, char offerTileChar) {
        throw new UnsupportedOperationException("Not implemented yet in base protocol.");
    }

    @Override
    public void takeCards(String playerId, SelectedCards selectedCards) {
        throw new UnsupportedOperationException("Not implemented yet in base protocol.");
    }

    @Override
    public void takeExtraCard(String playerId, int index) {
        throw new UnsupportedOperationException("Not implemented yet in base protocol.");
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

    private List<TurnOrderTile> defaultTurnOrderTiles() {
        List<TurnOrderTile> tiles = new ArrayList<>();
        for (int players = 2; players <= 5; players++) {
            List<TurnOrderSlot> slots = new ArrayList<>();
            for (int i = 0; i < players; i++) {
                slots.add(new TurnOrderSlot(i, 0, null));
            }
            tiles.add(new TurnOrderTile(slots));
        }
        return tiles;
    }

    private record LobbyRoom(LobbyState state) {
        private List<String> memberIds() {
            return state.getPlayers().stream().map(PlayerConnectionInfo::getId).toList();
        }
    }
}
