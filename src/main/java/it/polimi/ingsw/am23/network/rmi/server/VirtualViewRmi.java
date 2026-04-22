package it.polimi.ingsw.am23.network.rmi.server;

import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.state.GameState;
import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.VirtualView;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


public interface VirtualViewRmi extends VirtualView, Remote {

    @Override
    void onConnected(String playerId, List<LobbyState> lobbies) throws RemoteException;

    @Override
    void onConnectError(String reason) throws RemoteException;

    @Override
    void onLobbyListUpdated(List<LobbyState> lobbies) throws RemoteException;

    @Override
    void onLobbyCreated(LobbyState lobby) throws RemoteException;

    @Override
    void onLobbyUpdate(LobbyState lobby) throws RemoteException;

    @Override
    void onJoinError(String reason) throws RemoteException;

    @Override
    void onLobbyClosed() throws RemoteException;

    @Override
    void onGameStarted(GameState gameState) throws RemoteException;

    @Override
    void onGameStateChanged(GameState gameState) throws RemoteException;

    @Override
    void onEndOfPlacingPhase(GameState gameState) throws RemoteException;

    @Override
    void onEndOfDrawingPhase(GameState gameState) throws RemoteException;

    @Override
    void onExtraDrawRequest(GameState gameState) throws RemoteException;

    @Override
    void onEndOfResolvingPhase(GameState gameState) throws RemoteException;

    @Override
    void onEraProgression(GameState gameState) throws RemoteException;

    @Override
    void onGameOver(GameState gameState) throws RemoteException;

    @Override
    void onScoreboardAvailable(GameState gameState) throws RemoteException;

    @Override
    void onActionError(ActionType actionType, String message) throws RemoteException;
}