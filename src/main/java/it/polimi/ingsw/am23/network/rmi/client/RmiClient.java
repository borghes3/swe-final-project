package it.polimi.ingsw.am23.network.rmi.client;

import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.payloads.*;
import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.VirtualView;
import it.polimi.ingsw.am23.network.rmi.server.VirtualViewRmi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RMI implementation of the {@link VirtualViewRmi} remote view.
 * Wraps a local {@link VirtualView} and dispatches every server callback
 * onto a single-threaded executor to keep the RMI thread non blocking.
 * <p>
 * The client object is exported on the {@link #lastBoundCallbackPort} specified
 * by the client with the --rmiCallback flag. If not specified, 0 is passed and RMI chooses
 * a random port, otherwise it uses the specified port (for restricted NAT connections).
 */
public final class RmiClient extends UnicastRemoteObject implements VirtualViewRmi {

    /**
     * Local TCP port the callback object was bound to during the last
     * successful {@link #connect} call. Captured from the custom
     * {@link RMISocketFactory} so it reflects the real port even when the
     * caller passed {@code 0} (which asks RMI for a free random port).
     */
    private static volatile int lastBoundCallbackPort = -1;
    private static volatile HeartbeatService activeHeartbeatService;

    private final VirtualView view;
    private final VirtualServerRmi server;
    private HeartbeatService heartbeatService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Builds a new RMI client wrapping the supplied local view and
     * exports the callback object on the requested port.
     *
     * @param view         local view receiving the dispatched callbacks
     * @param server       remote server used by the heartbeat service
     * @param callbackPort TCP port used by the server to deliver callbacks;
     *                     {@code 0} lets RMI pick a free random port
     * @throws RemoteException if the underlying {@link UnicastRemoteObject}
     *                         export fails
     */
    public RmiClient(VirtualView view, VirtualServerRmi server, int callbackPort) throws RemoteException {
        super(callbackPort);
        this.view = Objects.requireNonNull(view, "view cannot be null");
        this.server = Objects.requireNonNull(server, "server cannot be null");
    }

    /**
     * @return the local TCP port the callback object was bound to during the
     * last successful {@link #connect} call, or {@code -1} if the
     * client never connected
     */
    public static int getLastBoundCallbackPort() {
        return lastBoundCallbackPort;
    }

    /**
     * Connects to the RMI server, registering this client as the player's
     * remote view.
     *
     * @param host         host of the RMI registry
     * @param playerName   desired display nickname
     * @param view         local view to wrap
     * @param callbackPort TCP port used by the server to deliver callbacks;
     *                     {@code 0} lets RMI pick a free random port
     * @return the remote server stub
     * @throws RemoteException   on transport failure
     * @throws NotBoundException if the server binding cannot be located
     */
    public static VirtualServerRmi connect(String host, String playerName, VirtualView view, int callbackPort)
            throws RemoteException, NotBoundException {

        try {
            RMISocketFactory.setSocketFactory(new RMISocketFactory() {
                @Override
                public Socket createSocket(String host, int port) throws IOException {
                    Socket s = new Socket();
                    s.connect(new InetSocketAddress(host, port), 2000);
                    s.setSoTimeout(5000);
                    return s;
                }

                @Override
                public ServerSocket createServerSocket(int port) throws IOException {
                    ServerSocket serverSocket = new ServerSocket(port);
                    lastBoundCallbackPort = serverSocket.getLocalPort();
                    return serverSocket;
                }
            });
        } catch (IOException ignored) {
        }

        final String serverName = "ServerName";
        Registry registry = LocateRegistry.getRegistry(host, 1234);
        VirtualServerRmi server = (VirtualServerRmi) registry.lookup(serverName);
        RmiClient client = new RmiClient(view, server, callbackPort);
        server.connect(playerName, client);
        return server;
    }

    /**
     * Receives the assigned player id, starts the RMI heartbeat and forwards
     * the connection event to the local view.
     *
     * @param playerId id assigned by the server
     * @param lobbies current lobby snapshot
     * @throws RemoteException on RMI transport failure
     */
    @Override
    public void onConnected(String playerId, List<LobbyState> lobbies) throws RemoteException {
        startHeartbeat(playerId);

        executor.submit(() -> {
            try {
                view.onConnected(playerId, lobbies);
            } catch (Exception ignored) {
            }
        });
    }


    /**
     * Starts the heartbeat for this RMI client if it has not been started yet.
     *
     * @param playerId id assigned by the server to this client
     */
    private void startHeartbeat(String playerId) {
        if (heartbeatService != null) {
            return;
        }

        heartbeatService = new HeartbeatService(server, playerId, () -> {
            try {
                view.onServerCrashed();
            } catch (Exception ignored) {
            }
        });

        activeHeartbeatService = heartbeatService;
        heartbeatService.start();
    }

    /**
     * Stops the currently active RMI heartbeat, if any.
     */
    public static void stopHeartbeat() {
        HeartbeatService service = activeHeartbeatService;

        if (service != null) {
            service.stop();
            activeHeartbeatService = null;
        }
    }

    @Override
    public void onConnectError(String reason) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onConnectError(reason);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onLobbyListUpdated(List<LobbyState> lobbies) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onLobbyListUpdated(lobbies);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onLobbyCreated(LobbyState lobby) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onLobbyCreated(lobby);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onLobbyUpdate(LobbyState lobby) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onLobbyUpdate(lobby);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onJoinError(String reason) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onJoinError(reason);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onLobbyClosed() throws RemoteException {
        executor.submit(() -> {
            try {
                view.onLobbyClosed();
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onGameStarted(GameStartedPayload payload) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onGameStarted(payload);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onTotemPlaced(TotemPlacedPayload payload) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onTotemPlaced(payload);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onEndOfPlacingPhase(EndOfPlacingPhasePayload payload) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onEndOfPlacingPhase(payload);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onCardsTaken(CardsTakenPayload payload) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onCardsTaken(payload);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onExtraDrawRequest(ExtraDrawRequestPayload payload) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onExtraDrawRequest(payload);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onExtraCardTaken(ExtraCardTakenPayload payload) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onExtraCardTaken(payload);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onEventResolved(EventResolvedPayload payload) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onEventResolved(payload);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onMarketRefreshed(MarketRefresherPayload payload) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onMarketRefreshed(payload);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onEraProgression(EraProgressionPayload payload) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onEraProgression(payload);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onGameOver() throws RemoteException {
        executor.submit(() -> {
            try {
                view.onGameOver();
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onScoreboardAvailable(ScoreBoardPayload payload) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onScoreboardAvailable(payload);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onMatchRankingsAvailable(MatchRankingsPayload payload) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onMatchRankingsAvailable(payload);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onLeaderboardAvailable(LeaderboardPayload payload) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onLeaderboardAvailable(payload);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onActionError(ActionType actionType, String message) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onActionError(actionType, message);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onServerCrashed() throws RemoteException {
        executor.submit(() -> {
            try {
                view.onServerCrashed();
            } catch (Exception ignored) {
            }
        });
    }
}