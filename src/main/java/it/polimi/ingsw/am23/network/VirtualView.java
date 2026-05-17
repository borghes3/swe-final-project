package it.polimi.ingsw.am23.network;

import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.payloads.*;
import it.polimi.ingsw.am23.model.payloads.ScoreBoardPayload;

import java.util.List;

public interface VirtualView {

    // connection
    void onConnected(String playerId, List<LobbyState> lobbies) throws Exception;

    // 'reason' = error's motivation (eg. "nome già in uso")
    void onConnectError(String reason) throws Exception;

    // lobby
    void onLobbyListUpdated(List<LobbyState> lobbies) throws Exception;


    // waiting room

    // sent only to the creator
    void onLobbyCreated(LobbyState lobby) throws Exception;

    // some change happened in the lobby
    void onLobbyUpdate(LobbyState lobby) throws Exception;

    void onJoinError(String reason) throws Exception;

    // creator left the lobby -> lobby destroyed (?)
    void onLobbyClosed() throws Exception;


    // setup
    void onGameStarted(GameStartedPayload payload) throws Exception;


    // game
    void onTotemPlaced(TotemPlacedPayload payload) throws Exception;

    void onEndOfPlacingPhase(EndOfPlacingPhasePayload payload) throws Exception;

    void onCardsTaken(CardsTakenPayload payload) throws Exception;

    void onExtraDrawRequest(ExtraDrawRequestPayload payload) throws Exception;

    void onExtraCardTaken(ExtraCardTakenPayload payload) throws Exception;

    void onEventResolved(EventResolvedPayload payload) throws Exception;

    void onMarketRefreshed(MarketRefresherPayload payload) throws Exception;

    void onEraProgression(EraProgressionPayload payload) throws Exception;

    void onGameOver() throws Exception;

    void onScoreboardAvailable(ScoreBoardPayload payload) throws Exception;

    // errors (eg. action not allowed)
    void onActionError(ActionType actionType, String message) throws Exception;

    void onServerCrashed() throws Exception;
}

