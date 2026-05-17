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
    public void onGameStarted(GameStartedPayload payload) throws RemoteException {
        try { view.onGameStarted(payload); } catch (Exception e) { throw new RemoteException(e.getMessage(), e); }
    }

    @Override
    public void onTotemPlaced(TotemPlacedPayload payload) throws RemoteException {
        try { view.onTotemPlaced(payload); } catch (Exception e) { throw new RemoteException(e.getMessage(), e); }
    }

    @Override
    public void onEndOfPlacingPhase(EndOfPlacingPhasePayload payload) throws RemoteException {
        try { view.onEndOfPlacingPhase(payload); } catch (Exception e) { throw new RemoteException(e.getMessage(), e); }
    }
    @Override
    public void onCardsTaken(CardsTakenPayload payload) throws RemoteException {
        try { view.onCardsTaken(payload); } catch (Exception e) { throw new RemoteException(e.getMessage(), e); }
    }

    @Override
    public void onExtraDrawRequest(ExtraDrawRequestPayload payload) throws RemoteException {
        try { view.onExtraDrawRequest(payload); } catch (Exception e) { throw new RemoteException(e.getMessage(), e); }
    }

    @Override
    public void onExtraCardTaken(ExtraCardTakenPayload payload) throws RemoteException {
        try { view.onExtraCardTaken(payload); } catch (Exception e) { throw new RemoteException(e.getMessage(), e); }
    }

    @Override
    public void onEventResolved(EventResolvedPayload payload) throws RemoteException {
        try { view.onEventResolved(payload); } catch (Exception e) { throw new RemoteException(e.getMessage(), e); }
    }

    @Override
    public void onMarketRefreshed(MarketRefresherPayload payload) throws RemoteException {
        try { view.onMarketRefreshed(payload); } catch (Exception e) { throw new RemoteException(e.getMessage(), e); }
    }

    @Override
    public void onEraProgression(EraProgressionPayload payload) throws RemoteException {
        try { view.onEraProgression(payload); } catch (Exception e) { throw new RemoteException(e.getMessage(), e); }
    }

    @Override
    public void onGameOver() throws RemoteException {
        try { view.onGameOver(); } catch (Exception e) { throw new RemoteException(e.getMessage(), e); }
    }

    @Override public void onScoreboardAvailable(ScoreBoardPayload payload) throws RemoteException {
        try { view.onScoreboardAvailable(payload); } catch (Exception e) { throw new RemoteException(e.getMessage(), e); }
    }

    @Override
    public void onActionError(ActionType actionType, String message) throws RemoteException {
        try { view.onActionError(actionType, message); } catch (Exception e) { throw new RemoteException(e.getMessage(), e); }
    }

    @Override
    public void onServerCrashed() throws RemoteException {
        try { view.onServerCrashed(); } catch (Exception e) { throw new RemoteException(e.getMessage(), e); }
    }

}