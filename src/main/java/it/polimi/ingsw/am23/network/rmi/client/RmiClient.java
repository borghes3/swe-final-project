package it.polimi.ingsw.am23.network.rmi.client;

import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.state.GameState;
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

public final class RmiClient extends UnicastRemoteObject implements VirtualViewRmi {

    private final VirtualView view;

    public RmiClient(VirtualView view) throws RemoteException {
        super();
        this.view = Objects.requireNonNull(view, "view cannot be null");
    }

    public static VirtualServerRmi connect(String host, String playerName, VirtualView view)
            throws RemoteException, NotBoundException {

        try{
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
        }catch(IOException ignored){}

        final String serverName = "ServerName";
        Registry registry = LocateRegistry.getRegistry(host, 1234);
        VirtualServerRmi server = (VirtualServerRmi) registry.lookup(serverName);
        RmiClient client = new RmiClient(view);
        server.connect(playerName, client);
        return server;
    }

    @Override
    public void onConnected(String playerId, List<LobbyState> lobbies) throws RemoteException {
        try {
            view.onConnected(playerId, lobbies);
        } catch (Exception exception) {
            throw new RemoteException(exception.getMessage(), exception);
        }
    }

    @Override
    public void onConnectError(String reason) throws RemoteException {
        try {
            view.onConnectError(reason);
        } catch (Exception exception) {
            throw new RemoteException(exception.getMessage(), exception);
        }
    }

    @Override
    public void onLobbyListUpdated(List<LobbyState> lobbies) throws RemoteException {
        try {
            view.onLobbyListUpdated(lobbies);
        } catch (Exception exception) {
            throw new RemoteException(exception.getMessage(), exception);
        }
    }

    @Override
    public void onLobbyCreated(LobbyState lobby) throws RemoteException {
        try {
            view.onLobbyCreated(lobby);
        } catch (Exception exception) {
            throw new RemoteException(exception.getMessage(), exception);
        }
    }

    @Override
    public void onLobbyUpdate(LobbyState lobby) throws RemoteException {
        try {
            view.onLobbyUpdate(lobby);
        } catch (Exception exception) {
            throw new RemoteException(exception.getMessage(), exception);
        }
    }

    @Override
    public void onJoinError(String reason) throws RemoteException {
        try {
            view.onJoinError(reason);
        } catch (Exception exception) {
            throw new RemoteException(exception.getMessage(), exception);
        }
    }

    @Override
    public void onLobbyClosed() throws RemoteException {
        try {
            view.onLobbyClosed();
        } catch (Exception exception) {
            throw new RemoteException(exception.getMessage(), exception);
        }
    }

    @Override
    public void onGameStarted(GameState gameState) throws RemoteException {
        try {
            view.onGameStarted(gameState);
        } catch (Exception exception) {
            throw new RemoteException(exception.getMessage(), exception);
        }
    }

    @Override
    public void onGameStateChanged(GameState gameState) throws RemoteException {
        try {
            view.onGameStateChanged(gameState);
        } catch (Exception exception) {
            throw new RemoteException(exception.getMessage(), exception);
        }
    }

    @Override
    public void onEndOfPlacingPhase(GameState gameState) throws RemoteException {
        try {
            view.onEndOfPlacingPhase(gameState);
        } catch (Exception exception) {
            throw new RemoteException(exception.getMessage(), exception);
        }
    }

    @Override
    public void onEndOfDrawingPhase(GameState gameState) throws RemoteException {
        try {
            view.onEndOfDrawingPhase(gameState);
        } catch (Exception exception) {
            throw new RemoteException(exception.getMessage(), exception);
        }
    }

    @Override
    public void onExtraDrawRequest(GameState gameState) throws RemoteException {
        try {
            view.onExtraDrawRequest(gameState);
        } catch (Exception exception) {
            throw new RemoteException(exception.getMessage(), exception);
        }
    }

    @Override
    public void onEndOfResolvingPhase(GameState gameState) throws RemoteException {
        try {
            view.onEndOfResolvingPhase(gameState);
        } catch (Exception exception) {
            throw new RemoteException(exception.getMessage(), exception);
        }
    }

    @Override
    public void onEraProgression(GameState gameState) throws RemoteException {
        try {
            view.onEraProgression(gameState);
        } catch (Exception exception) {
            throw new RemoteException(exception.getMessage(), exception);
        }
    }

    @Override
    public void onGameOver(GameState gameState) throws RemoteException {
        try {
            view.onGameOver(gameState);
        } catch (Exception exception) {
            throw new RemoteException(exception.getMessage(), exception);
        }
    }

    @Override
    public void onScoreboardAvailable(GameState gameState) throws RemoteException {
        try {
            view.onScoreboardAvailable(gameState);
        } catch (Exception exception) {
            throw new RemoteException(exception.getMessage(), exception);
        }
    }

    @Override
    public void onActionError(ActionType actionType, String message) throws RemoteException {
        try {
            view.onActionError(actionType, message);
        } catch (Exception exception) {
            throw new RemoteException(exception.getMessage(), exception);
        }
    }

    @Override
    public void onServerCrashed() throws RemoteException{
        try{
            view.onServerCrashed();
        } catch (Exception exception) {
            throw new RemoteException(exception.getMessage(), exception);
        }
    }
}