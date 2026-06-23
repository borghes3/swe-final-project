package it.polimi.ingsw.am23.controller;

import it.polimi.ingsw.am23.jsonParsing.Parser;
import it.polimi.ingsw.am23.model.ActionResult;
import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.ModelObserver;
import it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.payloads.*;
import it.polimi.ingsw.am23.model.setup.PlayerConnectionInfo;
import it.polimi.ingsw.am23.model.setup.Setup;
import it.polimi.ingsw.am23.network.LobbyPhase;
import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.VirtualServer;
import it.polimi.ingsw.am23.network.VirtualView;
import it.polimi.ingsw.am23.persistence.DatabaseConfig;
import it.polimi.ingsw.am23.persistence.LeaderboardRepository;
import it.polimi.ingsw.am23.persistence.RankingEntry;

import java.time.LocalDateTime;
import java.util.*;

/**
 * MVC controller that bridges the network layer and the model.
 * Manages the lobby lifecycle, the match lifecycle and the persistence of
 * leaderboard data. Implements {@link VirtualServer} to expose the actions
 * the clients can invoke and {@link ModelObserver} to forward model
 * notifications to every interested {@link VirtualView}.
 */
public final class GameController implements VirtualServer, ModelObserver {

    private static final int DEFAULT_LOBBY_MAX_PLAYERS = 5;
    private static final int LOBBY_CODE_LENGTH = 4;
    private static final String LOBBY_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LEADERBOARD_TEASER_LIMIT = 10;
    private static final int LEADERBOARD_FULL_LIMIT = 100;
    private final Map<String, VirtualView> clientsByPlayerId = new HashMap<>();
    private final Map<String, PlayerConnectionInfo> playersById = new HashMap<>();
    private final Map<String, String> lobbyByPlayerId = new HashMap<>();
    private final Map<String, LobbyRoom> lobbiesById = new LinkedHashMap<>();
    private final Map<String, Game> gamesByLobbyId = new HashMap<>();
    private final Random random = new Random();
    private final Set<String> scoredLobbyIds = new HashSet<>();
    private final LeaderboardRepository leaderboard;
    private String activeLobbyId;

    /**
     * Builds a new controller, initializing the leaderboard repository
     * from the persistence configuration loaded from classpath.
     */
    public GameController() {
        this.leaderboard = new LeaderboardRepository(DatabaseConfig.load());
        this.leaderboard.init();
    }

    /**
     * {@inheritDoc}
     */
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
                .anyMatch(p -> p.nickname().equalsIgnoreCase(normalizedName));
        if (nicknameAlreadyUsed) {
            client.onConnectError("Player name already in use.");
            return;
        }

        String playerId = UUID.randomUUID().toString();
        PlayerConnectionInfo info = new PlayerConnectionInfo(playerId, normalizedName);
        playersById.put(playerId, info);
        clientsByPlayerId.put(playerId, client);

        client.onConnected(playerId, currentLobbyStates());
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
        broadcastLobbyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void joinLobby(String playerId, String lobbyId) throws Exception {
        PlayerConnectionInfo player = requireConnectedPlayer(playerId);
        LobbyRoom lobby = requireLobby(lobbyId);

        if (lobby.state.isFull()) {
            clientsByPlayerId.get(playerId).onJoinError("Lobby is full.");
            return;
        }

        if (lobby.state.getLobbyPhase() != LobbyPhase.OPEN) {
            clientsByPlayerId.get(playerId).onJoinError("Lobby is closed.");
            return;
        }

        boolean alreadyInside = lobby.state.getPlayers().stream()
                .anyMatch(p -> p.id().equals(playerId));
        if (alreadyInside) {
            return;
        }

        lobby.state.addPlayer(player);
        lobbyByPlayerId.put(playerId, lobbyId);
        broadcastLobbyUpdate(lobby);
        broadcastLobbyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void requestLobbyList(String playerId) throws Exception {
        requireConnectedPlayer(playerId);
        VirtualView client = clientsByPlayerId.get(playerId);
        if (client != null) {
            client.onLobbyListUpdated(currentLobbyStates());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void leaveLobby(String playerId, String lobbyId) throws Exception {
        requireConnectedPlayer(playerId);
        LobbyRoom lobby = requireLobby(lobbyId);

        lobby.state.removePlayer(playerId);
        lobbyByPlayerId.remove(playerId);

        VirtualView leavingView = clientsByPlayerId.get(playerId);
        if (leavingView != null)
            leavingView.onLobbyClosed();

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

    /**
     * {@inheritDoc}
     */
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
        requireLobby(lobbyId).state.setLobbyPhase(LobbyPhase.CLOSE);
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
    public synchronized void takeSingleCard(String playerId, SelectedSingleCard selectedSingleCard) throws Exception {
        String lobbyId = requireLobbyIdForPlayer(playerId);
        Game game = requireGame(lobbyId);
        ActionResult result = withActiveLobby(lobbyId, () -> game.takeSingleCard(playerId, selectedSingleCard));
        handleActionResult(playerId, result);
        if (result.isSuccess()) {
            advanceGameFlow(lobbyId);
        }
    }

    @Override
    public synchronized void skipTurn(String playerId) throws Exception {
        String lobbyId = requireLobbyIdForPlayer(playerId);
        Game game = requireGame(lobbyId);
        ActionResult result = withActiveLobby(lobbyId, () -> game.skipTurn(playerId));
        handleActionResult(playerId, result);
        if (result.isSuccess()) {
            advanceGameFlow(lobbyId);
        }
    }

    @Override
    public synchronized void disconnect(String playerId) throws Exception {
        if (playerId == null || !playersById.containsKey(playerId)) {
            return;
        }

        String lobbyId = lobbyByPlayerId.get(playerId);
        if (lobbyId == null) {
            // player connected, but not inside a lobby
            clientsByPlayerId.remove(playerId);
            playersById.remove(playerId);
            return;
        }

        LobbyRoom lobby = requireLobby(lobbyId);

        Game game = gamesByLobbyId.get(lobbyId);
        boolean gameInProgress = game != null && game.getGamePhase() != GamePhase.ENDED;

        lobby.state.removePlayer(playerId);
        lobbyByPlayerId.remove(playerId);
        clientsByPlayerId.remove(playerId);
        playersById.remove(playerId);

        if (gameInProgress) {
            // a player disconnected during the match: the match is cancelled for everyone,
            // the lobby is removed, so the remaining players return to a clean lobby list
            for (String memberId : lobby.memberIds()) {
                VirtualView view = clientsByPlayerId.get(memberId);
                if (view != null) {
                    try { view.onLobbyClosed(); } catch (Exception ignored) {}
                }
            }
            destroyLobby(lobbyId);
            return;
        }

        if (game != null) {
            // match ALREADY ENDED: the others are still watching the leaderboard,
            // we don't interrupt them, we only clean up when the last player leaves
            if (lobby.state.getCurrentPlayers() == 0) {
                destroyLobby(lobbyId);
            }
            return;
        }

        if (lobby.state.getCurrentPlayers() == 0) {
            lobbiesById.remove(lobbyId);
            broadcastLobbyList();
            return;
        }

        if (Objects.equals(lobby.state.getOwnerPlayerId(), playerId)) {
            for (String memberId : lobby.memberIds()) {
                lobbyByPlayerId.remove(memberId);
                VirtualView view = clientsByPlayerId.get(memberId);
                if (view != null) {
                    try {
                        view.onLobbyClosed();
                    } catch (Exception ignored) {
                    }
                }
            }
            lobbiesById.remove(lobbyId);
            broadcastLobbyList();
            return;
        }

        broadcastLobbyUpdate(lobby);
        broadcastLobbyList();
    }

    @Override
    public synchronized void takeExtraCard(String playerId, SelectedCardExtraDraw selectedCardExtraDraw) throws Exception {
        String lobbyId = requireLobbyIdForPlayer(playerId);
        Game game = requireGame(lobbyId);
        ActionResult result = withActiveLobby(lobbyId, () -> game.takeExtraCard(playerId, selectedCardExtraDraw));
        handleActionResult(playerId, result);
        if (result.isSuccess()) {
            advanceGameFlow(lobbyId);
        }
    }

    @Override
    public synchronized void onGameStarted(GameStartedPayload payload) {
        broadcastToLobby(activeLobbyId, view -> view.onGameStarted(payload));
    }

    @Override
    public synchronized void onTotemPlaced(TotemPlacedPayload payload) {
        broadcastToLobby(activeLobbyId, view -> view.onTotemPlaced(payload));
    }

    @Override
    public synchronized void onEndOfPlacingPhase(EndOfPlacingPhasePayload payload) {
        broadcastToLobby(activeLobbyId, view -> view.onEndOfPlacingPhase(payload));
    }

    @Override
    public synchronized void onCardsTaken(CardsTakenPayload payload) {
        broadcastToLobby(activeLobbyId, view -> view.onCardsTaken(payload));
    }

    @Override
    public synchronized void onExtraDrawRequest(ExtraDrawRequestPayload payload) {
        broadcastToLobby(activeLobbyId, view -> view.onExtraDrawRequest(payload));
    }

    @Override
    public synchronized void onExtraCardTaken(ExtraCardTakenPayload payload) {
        broadcastToLobby(activeLobbyId, view -> view.onExtraCardTaken(payload));
    }

    @Override
    public synchronized void onEventResolved(EventResolvedPayload payload) {
        broadcastToLobby(activeLobbyId, view -> view.onEventResolved(payload));
    }

    @Override
    public synchronized void onMarketRefreshed(MarketRefresherPayload payload) {
        broadcastToLobby(activeLobbyId, view -> view.onMarketRefreshed(payload));
    }

    @Override
    public synchronized void onEraProgression(EraProgressionPayload payload) {
        broadcastToLobby(activeLobbyId, view -> view.onEraProgression(payload));
    }

    @Override
    public synchronized void onGameOver() {
        broadcastToLobby(activeLobbyId, VirtualView::onGameOver);
    }

    @Override
    public synchronized void onScoreboardAvailable(ScoreBoardPayload payload) {
        String finishedLobbyId = activeLobbyId;
        broadcastToLobby(finishedLobbyId, view -> view.onScoreboardAvailable(payload));
        persistAndBroadcastRankings(finishedLobbyId, payload);
        dismissFinishedLobby(finishedLobbyId);
    }

    private void dismissFinishedLobby(String lobbyId) {
        if (lobbyId == null) return;
        gamesByLobbyId.remove(lobbyId);
        scoredLobbyIds.remove(lobbyId);
        if (Objects.equals(activeLobbyId, lobbyId)) activeLobbyId = null;
        LobbyRoom lobby = lobbiesById.remove(lobbyId);
        Set<String> finishedMembers = new HashSet<>();
        if (lobby != null) {
            for (String memberId : lobby.memberIds()) {
                lobbyByPlayerId.remove(memberId);
                finishedMembers.add(memberId);
            }
        }
        try {
            broadcastLobbyListExcept(finishedMembers);
        } catch (Exception ignored) {}
    }

    private void broadcastLobbyListExcept(Set<String> skipPlayerIds) throws Exception {
        List<LobbyState> lobbyStates = currentLobbyStates();
        for (Map.Entry<String, VirtualView> entry : clientsByPlayerId.entrySet()) {
            String pid = entry.getKey();
            if (lobbyByPlayerId.containsKey(pid)) continue;   // already in another lobby
            if (skipPlayerIds.contains(pid)) continue;         // just left the finished match
            entry.getValue().onLobbyListUpdated(lobbyStates);
        }
    }

    private void persistAndBroadcastRankings(String lobbyId, ScoreBoardPayload payload) {
        if (lobbyId == null || payload == null || payload.scores() == null || payload.scores().isEmpty()) {
            return;
        }
        int playerCount = payload.scores().size();
        LocalDateTime now = LocalDateTime.now();

        for (PlayerScore s : payload.scores()) {
            String nickname = s.nickname() != null ? s.nickname() : s.playerId();
            leaderboard.saveResult(nickname, s.totalPrestigePoints(), playerCount, now);
        }

        Map<String, Integer> positionByPlayer = new HashMap<>();
        for (PlayerScore s : payload.scores()) {
            int pos = leaderboard.positionOf(s.totalPrestigePoints(), playerCount);
            positionByPlayer.put(s.playerId(), pos);
        }
        List<RankingEntry> teaser = leaderboard.topForPlayerCount(playerCount, LEADERBOARD_TEASER_LIMIT);

        MatchRankingsPayload rankings = new MatchRankingsPayload(
                playerCount,
                positionByPlayer,
                teaser,
                leaderboard.isAvailable()
        );
        broadcastToLobby(lobbyId, view -> view.onMatchRankingsAvailable(rankings));
    }

    @Override
    public synchronized void requestLeaderboard(String playerId, int playerCount) throws Exception {
        VirtualView view = clientsByPlayerId.get(playerId);
        if (view == null) return;

        List<RankingEntry> entries = playerCount > 0
                ? leaderboard.topForPlayerCount(playerCount, LEADERBOARD_FULL_LIMIT)
                : List.of();
        view.onLeaderboardAvailable(new LeaderboardPayload(playerCount, entries, leaderboard.isAvailable()));
    }

    @Override
    public synchronized void ping() {
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

        GamePhase phase = game.getGamePhase();

        if (phase == GamePhase.RESOLVING_EVENTS) {
            withActiveLobby(lobbyId, game::resolveEvents);

            if (game.getGamePhase() == GamePhase.ENDED) {
                calculateScoresOnce(lobbyId, game);
            }

            return;
        }

        if (phase == GamePhase.ENDED) {
            calculateScoresOnce(lobbyId, game);
        }
    }

    private void calculateScoresOnce(String lobbyId, Game game) throws Exception {
        if (scoredLobbyIds.contains(lobbyId)) {
            return;
        }

        scoredLobbyIds.add(lobbyId);
        withActiveLobby(lobbyId, game::calculateScores);
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
        for (Map.Entry<String, VirtualView> entry : clientsByPlayerId.entrySet()) {
            String playerId = entry.getKey();

            // players already inside a lobby do not receive lobby-list notifications
            if (lobbyByPlayerId.containsKey(playerId)) {
                continue;
            }

            entry.getValue().onLobbyListUpdated(lobbyStates);
        }
    }

    private List<LobbyState> currentLobbyStates() {
        // we only show lobbies that are still open: those that are in progress or already finished cannot be accessed
        return lobbiesById.values().stream()
                .filter(lobby -> lobby.state.getLobbyPhase() == LobbyPhase.OPEN)
                .map(lobby -> copyLobby(lobby.state))
                .toList();
    }

    /**
     * Permanently ends and removes a lobby (finished or cancelled match):
     * deletes the match and the lobby, removes members from the membership map, and
     * updates the lobby list for everyone, so the lobby is no longer visible.
     */
    private void destroyLobby(String lobbyId) {
        if (lobbyId == null) {
            return;
        }
        gamesByLobbyId.remove(lobbyId);
        scoredLobbyIds.remove(lobbyId);
        if (Objects.equals(activeLobbyId, lobbyId)) {
            activeLobbyId = null;
        }
        LobbyRoom lobby = lobbiesById.remove(lobbyId);
        if (lobby != null) {
            for (String memberId : lobby.memberIds()) {
                lobbyByPlayerId.remove(memberId);
            }
        }
        try {
            broadcastLobbyList();
        } catch (Exception ignored) {}
    }

    private void broadcastToLobby(String lobbyId, RemoteViewAction action) {
        if (lobbyId == null) return;
        LobbyRoom lobby = lobbiesById.get(lobbyId);
        if (lobby == null) return;
        for (String memberId : lobby.memberIds()) {
            VirtualView view = clientsByPlayerId.get(memberId);
            if (view != null) {
                try {
                    action.apply(view);
                } catch (Exception ignored) {
                }
            }
        }
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
        copy.setLobbyPhase(source.getLobbyPhase());
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

    @FunctionalInterface
    private interface GameAction {
        ActionResult run() throws Exception;
    }

    @FunctionalInterface
    private interface RemoteViewAction {
        void apply(VirtualView view) throws Exception;
    }

    private record LobbyRoom(LobbyState state) {
        private List<String> memberIds() {
            return state.getPlayers().stream().map(PlayerConnectionInfo::id).toList();
        }
    }
}