package it.polimi.ingsw.am23.network.rmi.server;

import it.polimi.ingsw.am23.model.cards.SelectedCards;
import it.polimi.ingsw.am23.network.VirtualServer;
import it.polimi.ingsw.am23.network.VirtualView;
import it.polimi.ingsw.am23.network.rmi.client.VirtualServerRmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RmiServer extends UnicastRemoteObject implements VirtualServerRmi {

    private final VirtualServer serverController;

    public RmiServer(VirtualServer serverController) throws RemoteException {
        super();
        this.serverController = serverController;
    }

    public static void startRmiServer(VirtualServer serverController) throws RemoteException {
        final String serverName = "ServerName";
        VirtualServerRmi server = new RmiServer(serverController);
        Registry registry = LocateRegistry.createRegistry(1234);
        registry.rebind(serverName, server);
        System.out.println("RMI Server avviato");
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
    public void leaveLobby(String playerId, String lobbyId) throws RemoteException {
        try {
            serverController.leaveLobby(playerId, lobbyId);
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
    public void takeCards(String playerId, SelectedCards selectedCards) throws RemoteException {
        try {
            serverController.takeCards(playerId, selectedCards);
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
}