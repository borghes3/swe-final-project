package it.polimi.ingsw.am23.network.rmi;

import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.state.GameState;
import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.VirtualView;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;


public class RmiClient extends UnicastRemoteObject implements VirtualViewRmi {

    private final VirtualView clientController;

    public RmiClient(VirtualView clientController) throws RemoteException {
        super();
        this.clientController = clientController;
    }

    public static VirtualServerRmi connect(String host, String playerName, VirtualView clientController)
            throws RemoteException, NotBoundException {
        final String serverName = "ServerName";
        Registry registry = LocateRegistry.getRegistry(host, 1234);
        VirtualServerRmi server = (VirtualServerRmi) registry.lookup(serverName);
        RmiClient client = new RmiClient(clientController);
        server.connect(playerName, client);
        return server;
    }


    @Override
    public void onConnected(String playerId, List<LobbyState> lobbies) throws RemoteException {
        try { clientController.onConnected(playerId, lobbies); }
        catch (Exception e) { throw new RemoteException(e.getMessage()); }
    }

    @Override
    public void onConnectError(String reason) throws RemoteException {
        try { clientController.onConnectError(reason); }
        catch (Exception e) { throw new RemoteException(e.getMessage()); }
    }

    @Override
    public void onLobbyListUpdated(List<LobbyState> lobbies) throws RemoteException {
        try { clientController.onLobbyListUpdated(lobbies); }
        catch (Exception e) { throw new RemoteException(e.getMessage()); }
    }

    @Override
    public void onLobbyCreated(LobbyState lobby) throws RemoteException {
        try { clientController.onLobbyCreated(lobby); }
        catch (Exception e) { throw new RemoteException(e.getMessage()); }
    }

    @Override
    public void onLobbyUpdate(LobbyState lobby) throws RemoteException {
        try { clientController.onLobbyUpdate(lobby); }
        catch (Exception e) { throw new RemoteException(e.getMessage()); }
    }

    @Override
    public void onJoinError(String reason) throws RemoteException {
        try { clientController.onJoinError(reason); }
        catch (Exception e) { throw new RemoteException(e.getMessage()); }
    }

    @Override
    public void onLobbyClosed() throws RemoteException {
        try { clientController.onLobbyClosed(); }
        catch (Exception e) { throw new RemoteException(e.getMessage()); }
    }

    @Override
    public void onGameStarted(GameState gameState) throws RemoteException {
        try { clientController.onGameStarted(gameState); }
        catch (Exception e) { throw new RemoteException(e.getMessage()); }
    }

    @Override
    public void onGameStateChanged(GameState gameState) throws RemoteException {
        try { clientController.onGameStateChanged(gameState); }
        catch (Exception e) { throw new RemoteException(e.getMessage()); }
    }

    @Override
    public void onEndOfPlacingPhase(GameState gameState) throws RemoteException {
        try { clientController.onEndOfPlacingPhase(gameState); }
        catch (Exception e) { throw new RemoteException(e.getMessage()); }
    }

    @Override
    public void onEndOfDrawingPhase(GameState gameState) throws RemoteException {
        try { clientController.onEndOfDrawingPhase(gameState); }
        catch (Exception e) { throw new RemoteException(e.getMessage()); }
    }

    @Override
    public void onExtraDrawRequest(GameState gameState) throws RemoteException {
        try { clientController.onExtraDrawRequest(gameState); }
        catch (Exception e) { throw new RemoteException(e.getMessage()); }
    }

    @Override
    public void onEndOfResolvingPhase(GameState gameState) throws RemoteException {
        try { clientController.onEndOfResolvingPhase(gameState); }
        catch (Exception e) { throw new RemoteException(e.getMessage()); }
    }

    @Override
    public void onEraProgression(GameState gameState) throws RemoteException {
        try { clientController.onEraProgression(gameState); }
        catch (Exception e) { throw new RemoteException(e.getMessage()); }
    }

    @Override
    public void onGameOver() throws RemoteException {
        try { clientController.onGameOver(); }
        catch (Exception e) { throw new RemoteException(e.getMessage()); }
    }

    @Override
    public void onScoreboardAvailable() throws RemoteException {
        try { clientController.onScoreboardAvailable(); }
        catch (Exception e) { throw new RemoteException(e.getMessage()); }
    }

    @Override
    public void onActionError(ActionType actionType, String message) throws RemoteException {
        try { clientController.onActionError(actionType, message); }
        catch (Exception e) { throw new RemoteException(e.getMessage()); }
    }
}