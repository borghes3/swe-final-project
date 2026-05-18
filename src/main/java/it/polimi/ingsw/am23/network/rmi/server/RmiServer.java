package it.polimi.ingsw.am23.network.rmi.server;

import it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;
import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.payloads.CardsTakenPayload;
import it.polimi.ingsw.am23.model.payloads.EndOfPlacingPhasePayload;
import it.polimi.ingsw.am23.model.payloads.EraProgressionPayload;
import it.polimi.ingsw.am23.model.payloads.EventResolvedPayload;
import it.polimi.ingsw.am23.model.payloads.ExtraCardTakenPayload;
import it.polimi.ingsw.am23.model.payloads.ExtraDrawRequestPayload;
import it.polimi.ingsw.am23.model.payloads.GameStartedPayload;
import it.polimi.ingsw.am23.model.payloads.MarketRefresherPayload;
import it.polimi.ingsw.am23.model.payloads.ScoreBoardPayload;
import it.polimi.ingsw.am23.model.payloads.TotemPlacedPayload;
import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.VirtualServer;
import it.polimi.ingsw.am23.network.VirtualView;
import it.polimi.ingsw.am23.network.rmi.client.VirtualServerRmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RmiServer extends UnicastRemoteObject implements VirtualServerRmi {

    private static final int PORT = 1234;
    private static final String SERVER_NAME = "ServerName";

    private final VirtualServer serverController;

    /*
     * RMI method calls are kept asynchronous: the RMI thread only schedules
     * the operation, while the real application logic is executed by this pool.
     */
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /*
     * RMI server adapter needs this map because all methods after connect(...)
     * receive only playerId, not the VirtualView callback object.
     *
     * The map is filled by TrackingVirtualView when the controller calls
     * onConnected(playerId, ...).
     */
    private final Map<String, VirtualView> clientsByPlayerId = new ConcurrentHashMap<>();

    public RmiServer(VirtualServer serverController) throws RemoteException {
        super();
        this.serverController = serverController;
    }

    public static void startRmiServer(VirtualServer serverController) throws RemoteException {
        VirtualServerRmi server = new RmiServer(serverController);

        Registry registry = LocateRegistry.createRegistry(PORT);
        registry.rebind(SERVER_NAME, server);

        System.out.println("RMI Server avviato su porta " + PORT);
    }

    @Override
    public void connect(String playerName, VirtualView client) throws RemoteException {
        executor.execute(() -> {
            try {
                /*
                 * The controller generates the playerId during connect(...).
                 * We wrap the client so that we can capture that generated id
                 * and store playerId -> client for future error callbacks.
                 */
                serverController.connect(playerName, new TrackingVirtualView(client));

            } catch (Exception e) {
                logOperationError("connect", e);

                /*
                 * connect(...) is the only operation where we still have the
                 * client reference directly, even if playerId is not known yet.
                 */
                safeCallback("connect.onConnectError",
                        () -> client.onConnectError(messageOf(e)));
            }
        });
    }

    @Override
    public void createLobby(String playerId, String lobbyName, int maxPlayers) throws RemoteException {
        runAsyncForPlayer(
                "createLobby",
                playerId,
                ActionType.GENERIC,
                () -> serverController.createLobby(playerId, lobbyName, maxPlayers)
        );
    }

    @Override
    public void joinLobby(String playerId, String lobbyId) throws RemoteException {
        executor.execute(() -> {
            try {
                serverController.joinLobby(playerId, lobbyId);

            } catch (IllegalArgumentException e) {
                /*
                 * Joining a lobby has a specific error callback in the view
                 * protocol, so we use onJoinError(...) instead of a generic
                 * action error.
                 */
                logOperationError("joinLobby", e);
                notifyJoinError(playerId, e);

            } catch (Exception e) {
                logOperationError("joinLobby", e);
                notifyActionError(playerId, ActionType.GENERIC, e);
            }
        });
    }

    @Override
    public void requestLobbyList(String playerId) throws RemoteException {
        runAsyncForPlayer(
                "requestLobbyList",
                playerId,
                ActionType.GENERIC,
                () -> serverController.requestLobbyList(playerId)
        );
    }

    @Override
    public void leaveLobby(String playerId, String lobbyId) throws RemoteException {
        runAsyncForPlayer(
                "leaveLobby",
                playerId,
                ActionType.GENERIC,
                () -> serverController.leaveLobby(playerId, lobbyId)
        );
    }

    @Override
    public void startGame(String playerId, String lobbyId) throws RemoteException {
        runAsyncForPlayer(
                "startGame",
                playerId,
                ActionType.GENERIC,
                () -> serverController.startGame(playerId, lobbyId)
        );
    }

    @Override
    public void placeTotem(String playerId, char offerTileChar) throws RemoteException {
        runAsyncForPlayer(
                "placeTotem",
                playerId,
                ActionType.PLACE_TOTEM,
                () -> serverController.placeTotem(playerId, offerTileChar)
        );
    }

    @Override
    public void takeSingleCard(String playerId, SelectedSingleCard selectedSingleCard) throws RemoteException {
        runAsyncForPlayer(
                "takeSingleCard",
                playerId,
                ActionType.TAKE_CARD,
                () -> serverController.takeSingleCard(playerId, selectedSingleCard)
        );
    }

    @Override
    public void takeExtraCard(String playerId, SelectedCardExtraDraw selectedCardExtraDraw) throws RemoteException {
        runAsyncForPlayer(
                "takeExtraCard",
                playerId,
                ActionType.TAKE_CARD,
                () -> serverController.takeExtraCard(playerId, selectedCardExtraDraw)
        );
    }

    @Override
    public void skipTurn(String playerId) throws RemoteException {
        runAsyncForPlayer(
                "skipTurn",
                playerId,
                ActionType.SKIP_TURN,
                () -> serverController.skipTurn(playerId)
        );
    }

    @Override
    public void disconnect(String playerId) throws RemoteException {
        executor.execute(() -> {
            try {
                serverController.disconnect(playerId);

            } catch (Exception e) {
                logOperationError("disconnect", e);

            } finally {
                /*
                 * Remove the callback reference once the client disconnects,
                 * so the server does not keep stale remote references.
                 */
                clientsByPlayerId.remove(playerId);
            }
        });
    }

    @Override
    public void ping() throws RemoteException {
        /*
         * Intentionally empty.
         * Used only by the client to check whether the RMI server is reachable.
         */
    }

    /*
     * Common wrapper for asynchronous player operations.
     */
    private void runAsyncForPlayer(String operation,
                                   String playerId,
                                   ActionType actionType,
                                   ThrowingRunnable action) {
        executor.execute(() -> {
            try {
                action.run();

            } catch (Exception e) {
                logOperationError(operation, e);
                notifyActionError(playerId, actionType, e);
            }
        });
    }

    private void notifyActionError(String playerId, ActionType actionType, Exception e) {
        VirtualView client = clientsByPlayerId.get(playerId);

        if (client == null) {
            System.err.println("[RMI] Cannot notify action error. Unknown playerId: " + playerId);
            return;
        }

        safeCallback(
                "notifyActionError",
                () -> client.onActionError(actionType, messageOf(e))
        );
    }

    private void notifyJoinError(String playerId, Exception e) {
        VirtualView client = clientsByPlayerId.get(playerId);

        if (client == null) {
            System.err.println("[RMI] Cannot notify join error. Unknown playerId: " + playerId);
            return;
        }

        safeCallback(
                "notifyJoinError",
                () -> client.onJoinError(messageOf(e))
        );
    }

    /*
     * Executes a callback safely.
     *
     * Callback failures must not crash the RMI server adapter. They are logged,
     * because they usually mean the client disconnected or is no longer reachable.
     */
    private void safeCallback(String operation, ThrowingRunnable callback) {
        try {
            callback.run();

        } catch (Exception e) {
            logOperationError(operation, e);
        }
    }

    private void logOperationError(String operation, Exception e) {
        System.err.println("[RMI] Error during " + operation + ": " + messageOf(e));
        e.printStackTrace();
    }

    private String messageOf(Exception e) {
        if (e.getMessage() != null && !e.getMessage().isBlank()) {
            return e.getMessage();
        }

        return e.getClass().getSimpleName();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    /*
     * Wrapper around the original remote client.
     *
     * The purpose of this class is to intercept onConnected(...), because that
     * is the first moment in which the server-side RMI adapter knows the
     * generated playerId.
     *
     * All other methods are simply forwarded to the original client.
     */
    private final class TrackingVirtualView implements VirtualView {

        private final VirtualView client;

        private TrackingVirtualView(VirtualView client) {
            this.client = client;
        }

        @Override
        public void onConnected(String playerId, List<LobbyState> lobbies) throws Exception {
            clientsByPlayerId.put(playerId, client);
            client.onConnected(playerId, lobbies);
        }

        @Override
        public void onConnectError(String reason) throws Exception {
            client.onConnectError(reason);
        }

        @Override
        public void onLobbyListUpdated(List<LobbyState> lobbies) throws Exception {
            client.onLobbyListUpdated(lobbies);
        }

        @Override
        public void onLobbyCreated(LobbyState lobby) throws Exception {
            client.onLobbyCreated(lobby);
        }

        @Override
        public void onLobbyUpdate(LobbyState lobby) throws Exception {
            client.onLobbyUpdate(lobby);
        }

        @Override
        public void onJoinError(String reason) throws Exception {
            client.onJoinError(reason);
        }

        @Override
        public void onLobbyClosed() throws Exception {
            client.onLobbyClosed();
        }

        @Override
        public void onGameStarted(GameStartedPayload payload) throws Exception {
            client.onGameStarted(payload);
        }

        @Override
        public void onTotemPlaced(TotemPlacedPayload payload) throws Exception {
            client.onTotemPlaced(payload);
        }

        @Override
        public void onEndOfPlacingPhase(EndOfPlacingPhasePayload payload) throws Exception {
            client.onEndOfPlacingPhase(payload);
        }

        @Override
        public void onCardsTaken(CardsTakenPayload payload) throws Exception {
            client.onCardsTaken(payload);
        }

        @Override
        public void onExtraDrawRequest(ExtraDrawRequestPayload payload) throws Exception {
            client.onExtraDrawRequest(payload);
        }

        @Override
        public void onExtraCardTaken(ExtraCardTakenPayload payload) throws Exception {
            client.onExtraCardTaken(payload);
        }

        @Override
        public void onEventResolved(EventResolvedPayload payload) throws Exception {
            client.onEventResolved(payload);
        }

        @Override
        public void onMarketRefreshed(MarketRefresherPayload payload) throws Exception {
            client.onMarketRefreshed(payload);
        }

        @Override
        public void onEraProgression(EraProgressionPayload payload) throws Exception {
            client.onEraProgression(payload);
        }

        @Override
        public void onGameOver() throws Exception {
            client.onGameOver();
        }

        @Override
        public void onScoreboardAvailable(ScoreBoardPayload payload) throws Exception {
            client.onScoreboardAvailable(payload);
        }

        @Override
        public void onActionError(ActionType actionType, String message) throws Exception {
            client.onActionError(actionType, message);
        }

        @Override
        public void onServerCrashed() throws Exception {
            client.onServerCrashed();
        }
    }
}