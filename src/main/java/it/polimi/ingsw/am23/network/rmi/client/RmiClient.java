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
 * The client object is exported on the fixed {@link #CALLBACK_PORT} so the
 * port the server uses to deliver callbacks is predictable and can be
 * opened on firewalls / NATs without having to guess a random port.
 */
public final class RmiClient extends UnicastRemoteObject implements VirtualViewRmi {

    /**
     * TCP port used for the callbacks the server delivers to the client.
     */
    public static final int CALLBACK_PORT = 1236;

    private final VirtualView view;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Builds a new RMI client wrapping the supplied local view and
     * exports the callback object on {@link #CALLBACK_PORT}.
     *
     * @param view local view receiving the dispatched callbacks
     * @throws RemoteException if the underlying {@link UnicastRemoteObject}
     *                         export fails
     */
    public RmiClient(VirtualView view) throws RemoteException {
        super(CALLBACK_PORT);
        this.view = Objects.requireNonNull(view, "view cannot be null");
    }

    /**
     * Connects to the RMI server, registering this client as the player's
     * remote view.
     *
     * @param host       host of the RMI registry
     * @param playerName desired display nickname
     * @param view       local view to wrap
     * @return the remote server stub
     * @throws RemoteException   on transport failure
     * @throws NotBoundException if the server binding cannot be located
     */
    public static VirtualServerRmi connect(String host, String playerName, VirtualView view)
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
                    return new ServerSocket(port);
                }
            });
        } catch (IOException ignored) {
        }

        final String serverName = "ServerName";
        Registry registry = LocateRegistry.getRegistry(host, 1234);
        VirtualServerRmi server = (VirtualServerRmi) registry.lookup(serverName);
        RmiClient client = new RmiClient(view);
        server.connect(playerName, client);
        return server;
    }

    @Override
    public void onConnected(String playerId, List<LobbyState> lobbies) throws RemoteException {
        executor.submit(() -> {
            try {
                view.onConnected(playerId, lobbies);
            } catch (Exception ignored) {
            }
        });
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