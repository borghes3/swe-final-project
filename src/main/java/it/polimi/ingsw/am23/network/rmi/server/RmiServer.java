package it.polimi.ingsw.am23.network.rmi.server;

import it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;
import it.polimi.ingsw.am23.network.VirtualServer;
import it.polimi.ingsw.am23.network.VirtualView;
import it.polimi.ingsw.am23.network.rmi.client.VirtualServerRmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RMI adapter exposing the controller as a {@link VirtualServerRmi}.
 * Every remote call is delegated to the wrapped controller on a cached
 * thread pool so the RMI transport thread stays responsive.
 * <p>
 * Both the RMI registry and the exported remote object are bound to
 * {@link #PORT}, so opening that single TCP port on the server firewall
 * is enough to accept inbound RMI traffic.
 */
public class RmiServer extends UnicastRemoteObject implements VirtualServerRmi {

    /**
     * TCP port used for both the registry and the exported remote object.
     */
    public static final int PORT = 1234;
    private final VirtualServer serverController;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Builds the adapter and exports it on the well-known port.
     *
     * @param serverController controller the calls are delegated to
     * @throws RemoteException if the export fails
     */
    public RmiServer(VirtualServer serverController) throws RemoteException {
        super(PORT);
        this.serverController = serverController;
    }

    /**
     * Boots the RMI server, creating a registry and binding the adapter.
     *
     * @param serverController controller the calls are delegated to
     * @throws RemoteException if the registry creation or the bind fails
     */
    public static void startRmiServer(VirtualServer serverController) throws RemoteException {
        final String serverName = "ServerName";
        VirtualServerRmi server = new RmiServer(serverController);
        Registry registry = LocateRegistry.createRegistry(PORT);
        registry.rebind(serverName, server);
        System.out.println("RMI Server started on port " + PORT);
    }

    @Override
    public void connect(String playerName, VirtualView client) throws RemoteException {
        executor.submit(() -> {
            try {
                serverController.connect(playerName, client);
            } catch (Exception e) {
                try {
                    client.onConnectError(e.getMessage());
                } catch (Exception ignored) {
                }
            }
        });
    }

    @Override
    public void createLobby(String playerId, String lobbyName, int maxPlayers) throws RemoteException {
        executor.submit(() -> {
            try {
                serverController.createLobby(playerId, lobbyName, maxPlayers);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void joinLobby(String playerId, String lobbyId) throws RemoteException {
        executor.submit(() -> {
            try {
                serverController.joinLobby(playerId, lobbyId);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void requestLobbyList(String playerId) throws RemoteException {
        executor.submit(() -> {
            try {
                serverController.requestLobbyList(playerId);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void leaveLobby(String playerId, String lobbyId) throws RemoteException {
        executor.submit(() -> {
            try {
                serverController.leaveLobby(playerId, lobbyId);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void startGame(String playerId, String lobbyId) throws RemoteException {
        executor.submit(() -> {
            try {
                serverController.startGame(playerId, lobbyId);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void placeTotem(String playerId, char offerTileChar) throws RemoteException {
        executor.submit(() -> {
            try {
                serverController.placeTotem(playerId, offerTileChar);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void takeSingleCard(String playerId, SelectedSingleCard selectedSingleCard) throws RemoteException {
        executor.submit(() -> {
            try {
                serverController.takeSingleCard(playerId, selectedSingleCard);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void takeExtraCard(String playerId, SelectedCardExtraDraw selectedCardExtraDraw) throws RemoteException {
        executor.submit(() -> {
            try {
                serverController.takeExtraCard(playerId, selectedCardExtraDraw);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void skipTurn(String playerId) throws RemoteException {
        executor.submit(() -> {
            try {
                serverController.skipTurn(playerId);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void disconnect(String playerId) throws RemoteException {
        executor.submit(() -> {
            try {
                serverController.disconnect(playerId);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void requestLeaderboard(String playerId, int playerCount) throws RemoteException {
        executor.submit(() -> {
            try {
                serverController.requestLeaderboard(playerId, playerCount);
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void ping() throws RemoteException {
    }
}