package it.polimi.ingsw.am23.network.rmi.server;

import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.payloads.*;
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
    void onGameStarted(GameStartedPayload payload) throws RemoteException;

    @Override
    void onTotemPlaced(TotemPlacedPayload payload) throws RemoteException;

    @Override
    void onEndOfPlacingPhase(EndOfPlacingPhasePayload payload) throws RemoteException;

    @Override
    void onCardsTaken(CardsTakenPayload payload) throws RemoteException;

    @Override
    void onExtraDrawRequest(ExtraDrawRequestPayload payload) throws RemoteException;

    @Override
    void onExtraCardTaken(ExtraCardTakenPayload payload) throws RemoteException;

    @Override
    void onEventResolved(EventResolvedPayload payload) throws RemoteException;

    @Override
    void onMarketRefreshed(MarketRefresherPayload payload) throws RemoteException;

    @Override
    void onEraProgression(EraProgressionPayload payload) throws RemoteException;

    @Override
    void onGameOver() throws RemoteException;

    @Override
    void onScoreboardAvailable(ScoreBoardPayload payload) throws RemoteException;

    @Override
    void onMatchRankingsAvailable(MatchRankingsPayload payload) throws RemoteException;

    @Override
    void onLeaderboardAvailable(LeaderboardPayload payload) throws RemoteException;

    @Override
    void onActionError(ActionType actionType, String message) throws RemoteException;

    @Override
    void onServerCrashed() throws RemoteException;
}