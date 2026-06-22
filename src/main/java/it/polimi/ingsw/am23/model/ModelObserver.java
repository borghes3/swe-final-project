package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.model.payloads.*;

/**
 * Observer interface implemented by the controller (and ultimately the
 * network layer) to receive notifications about the model lifecycle.
 * Each callback corresponds to a specific event with a typed payload.
 */
public interface ModelObserver {
    /**
     * Notified when the game starts, carrying a full snapshot of the
     * initial state.
     *
     * @param payload the initial state payload
     */
    void onGameStarted(GameStartedPayload payload);

    /**
     * Notified when a totem is placed on an offer tile.
     *
     * @param payload the placement payload
     */
    void onTotemPlaced(TotemPlacedPayload payload);

    /**
     * Notified when all players complete the placing phase.
     *
     * @param payload the end-of-placing payload
     */
    void onEndOfPlacingPhase(EndOfPlacingPhasePayload payload);

    /**
     * Notified after a draw action completes (also for intermediate steps
     * within a multi-draw turn).
     *
     * @param payload the cards-taken payload
     */
    void onCardsTaken(CardsTakenPayload payload);

    /**
     * Notified when a player is invited to perform their extra draw.
     *
     * @param payload the request payload
     */
    void onExtraDrawRequest(ExtraDrawRequestPayload payload);

    /**
     * Notified after an extra draw card is taken.
     *
     * @param payload the extra card payload
     */
    void onExtraCardTaken(ExtraCardTakenPayload payload);

    /**
     * Notified after each event card is resolved.
     *
     * @param payload the event resolution payload
     */
    void onEventResolved(EventResolvedPayload payload);

    /**
     * Notified during the end-of-round market refresh.
     *
     * @param payload the market refresh payload
     */
    void onMarketRefreshed(MarketRefresherPayload payload);

    /**
     * Notified when the match progresses to a new era.
     *
     * @param payload the era progression payload
     */
    void onEraProgression(EraProgressionPayload payload);

    /** Notified when the match terminates. */
    void onGameOver();

    /**
     * Notified when the final scoreboard becomes available.
     *
     * @param payload the scoreboard payload
     */
    void onScoreboardAvailable(ScoreBoardPayload payload);

}
