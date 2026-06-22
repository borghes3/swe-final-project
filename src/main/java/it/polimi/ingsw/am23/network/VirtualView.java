package it.polimi.ingsw.am23.network;

import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.payloads.*;

import java.util.List;

/**
 * Remote view contract exposed by the client to the server.
 * Every method represents a notification the server can push to the client
 * to keep its UI in sync with the model.
 */
public interface VirtualView {

    /**
     * Notifies the client that the connection has been accepted.
     *
     * @param playerId unique identifier assigned to the player
     * @param lobbies  current snapshot of the lobby list
     * @throws Exception on transport failure
     */
    void onConnected(String playerId, List<LobbyState> lobbies) throws Exception;

    /**
     * Notifies the client that the connection has been rejected.
     *
     * @param reason human readable rejection reason (e.g. "nickname already in use")
     * @throws Exception on transport failure
     */
    void onConnectError(String reason) throws Exception;

    /**
     * Notifies the client about an updated snapshot of the lobby list.
     *
     * @param lobbies updated lobby list
     * @throws Exception on transport failure
     */
    void onLobbyListUpdated(List<LobbyState> lobbies) throws Exception;


    /**
     * Notifies the lobby creator that the lobby has been created.
     *
     * @param lobby the created lobby
     * @throws Exception on transport failure
     */
    void onLobbyCreated(LobbyState lobby) throws Exception;

    /**
     * Notifies that some change happened inside a lobby the client is in.
     *
     * @param lobby the updated lobby state
     * @throws Exception on transport failure
     */
    void onLobbyUpdate(LobbyState lobby) throws Exception;

    /**
     * Notifies that the requested join failed.
     *
     * @param reason human readable rejection reason
     * @throws Exception on transport failure
     */
    void onJoinError(String reason) throws Exception;

    /**
     * Notifies that the lobby has been closed (e.g. the owner left).
     *
     * @throws Exception on transport failure
     */
    void onLobbyClosed() throws Exception;


    /**
     * Notifies that the match has started, carrying the initial state.
     *
     * @param payload initial state payload
     * @throws Exception on transport failure
     */
    void onGameStarted(GameStartedPayload payload) throws Exception;


    /**
     * Notifies that a totem has been placed.
     *
     * @param payload placement payload
     * @throws Exception on transport failure
     */
    void onTotemPlaced(TotemPlacedPayload payload) throws Exception;

    /**
     * Notifies that the placing phase has ended.
     *
     * @param payload end-of-placing payload
     * @throws Exception on transport failure
     */
    void onEndOfPlacingPhase(EndOfPlacingPhasePayload payload) throws Exception;

    /**
     * Notifies that one or more cards have been drawn.
     *
     * @param payload cards-taken payload
     * @throws Exception on transport failure
     */
    void onCardsTaken(CardsTakenPayload payload) throws Exception;

    /**
     * Notifies that a player is invited to perform an extra draw.
     *
     * @param payload extra draw request payload
     * @throws Exception on transport failure
     */
    void onExtraDrawRequest(ExtraDrawRequestPayload payload) throws Exception;

    /**
     * Notifies that an extra draw has been completed.
     *
     * @param payload extra card payload
     * @throws Exception on transport failure
     */
    void onExtraCardTaken(ExtraCardTakenPayload payload) throws Exception;

    /**
     * Notifies that an event card has been resolved.
     *
     * @param payload event resolution payload
     * @throws Exception on transport failure
     */
    void onEventResolved(EventResolvedPayload payload) throws Exception;

    /**
     * Notifies that the market has been refreshed at end of round.
     *
     * @param payload market refresh payload
     * @throws Exception on transport failure
     */
    void onMarketRefreshed(MarketRefresherPayload payload) throws Exception;

    /**
     * Notifies that the match progressed to a new era.
     *
     * @param payload era progression payload
     * @throws Exception on transport failure
     */
    void onEraProgression(EraProgressionPayload payload) throws Exception;

    /**
     * Notifies that the match has ended.
     *
     * @throws Exception on transport failure
     */
    void onGameOver() throws Exception;

    /**
     * Notifies that the final scoreboard is available.
     *
     * @param payload scoreboard payload
     * @throws Exception on transport failure
     */
    void onScoreboardAvailable(ScoreBoardPayload payload) throws Exception;

    /**
     * Notifies that the global rankings related to the match just finished
     * are available.
     *
     * @param payload match rankings payload
     * @throws Exception on transport failure
     */
    void onMatchRankingsAvailable(MatchRankingsPayload payload) throws Exception;

    /**
     * Notifies that the requested leaderboard slice is available.
     *
     * @param payload leaderboard payload
     * @throws Exception on transport failure
     */
    void onLeaderboardAvailable(LeaderboardPayload payload) throws Exception;

    /**
     * Notifies that a previous action was rejected by the model.
     *
     * @param actionType type of the rejected action
     * @param message    human readable rejection reason
     * @throws Exception on transport failure
     */
    void onActionError(ActionType actionType, String message) throws Exception;

    /**
     * Notifies that the server side connection has been lost.
     *
     * @throws Exception on transport failure
     */
    void onServerCrashed() throws Exception;
}

