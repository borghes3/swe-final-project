package it.polimi.ingsw.am23.network;

import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.state.GameState;

import java.util.List;

public interface VirtualView {

    // connection
    void onConnected(String playerId, List<LobbyState> lobbies) throws Exception;

    // 'reason' = error's motivation (eg. "nome già in uso")
    void onConnectError(String reason) throws Exception;


    // lobby
    void onLobbyListUpdated(List<LobbyState> lobbies) throws Exception;


    // watiing room

    // sent only to the creator
    void onLobbyCreated(LobbyState lobby) throws Exception;

    // some change happened in the lobby
    void onLobbyUpdate(LobbyState lobby) throws Exception;

    void onJoinError(String reason) throws Exception;

    // creator left the lobby -> lobby destroyed (?)
    void onLobbyClosed() throws Exception;


    // setup
    void onGameStarted(GameState gameState) throws Exception;


    // game
    void onGameStateChanged(GameState gameState) throws Exception;

    void onEndOfPlacingPhase(GameState gameState) throws Exception;

    void onEndOfDrawingPhase(GameState gameState) throws Exception;

    void onExtraDrawRequest(GameState gameState) throws Exception;

    void onEndOfResolvingPhase(GameState gameState) throws Exception;

    void onEraProgression(GameState gameState) throws Exception;


    // end game
    void onGameOver() throws Exception;

    void onScoreboardAvailable() throws Exception;


    // errors (eg. action not allowed)
    void onActionError(ActionType actionType, String message) throws Exception;
}

