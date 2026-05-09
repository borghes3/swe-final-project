package it.polimi.ingsw.am23.network.rmi.server;

import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;
import it.polimi.ingsw.am23.network.VirtualServer;
import it.polimi.ingsw.am23.network.VirtualView;
import it.polimi.ingsw.am23.network.rmi.client.VirtualServerRmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RmiServer extends UnicastRemoteObject implements VirtualServerRmi {

    private final VirtualServer serverController;
    private static final int PORT = 1234;

    public RmiServer(VirtualServer serverController) throws RemoteException {
        super();
        this.serverController = serverController;
    }

    public static void startRmiServer(VirtualServer serverController) throws RemoteException {
        final String serverName = "ServerName";
        VirtualServerRmi server = new RmiServer(serverController);
        Registry registry = LocateRegistry.createRegistry(PORT);
        registry.rebind(serverName, server);
        System.out.println("RMI Server avviato su porta " + PORT);
    }

    @Override
    public void connect(String playerName, VirtualView client) throws RemoteException {
        try {
            serverController.connect(playerName, client);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void createLobby(String playerId, String lobbyName, int maxPlayers) throws RemoteException {
        try {
            serverController.createLobby(playerId, lobbyName, maxPlayers);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void joinLobby(String playerId, String lobbyId) throws RemoteException {
        try {
            serverController.joinLobby(playerId, lobbyId);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void requestLobbyList(String playerId) throws RemoteException {
        try {
            serverController.requestLobbyList(playerId);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void leaveLobby(String playerId, String lobbyId) throws RemoteException {
        try {
            serverController.leaveLobby(playerId, lobbyId);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void startGame(String playerId, String lobbyId) throws RemoteException {
        try {
            serverController.startGame(playerId, lobbyId);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void placeTotem(String playerId, char offerTileChar) throws RemoteException {
        try {
            serverController.placeTotem(playerId, offerTileChar);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void takeSingleCard(String playerId, SelectedSingleCard selectedSingleCard) throws RemoteException {
        try {
            serverController.takeSingleCard(playerId, selectedSingleCard);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void takeExtraCard(String playerId, int index) throws RemoteException {
        try {
            serverController.takeExtraCard(playerId, index);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void skipTurn(String playerId) throws RemoteException {
        try {
            serverController.skipTurn(playerId);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void disconnect(String playerId) throws RemoteException {
        try {
            serverController.disconnect(playerId);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public void ping() throws RemoteException {}
}