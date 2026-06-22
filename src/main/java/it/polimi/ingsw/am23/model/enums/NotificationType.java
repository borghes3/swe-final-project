package it.polimi.ingsw.am23.model.enums;

/**
 * Identifies the type of notification dispatched by the model to its
 * observers. Each value corresponds to a distinct payload class.
 */
public enum NotificationType {
    /**
     * The game has started and the initial state is available.
     */
    GAME_STARTED,
    /**
     * A totem was placed on an offer tile.
     */
    TOTEM_PLACED,
    /**
     * All players completed the placing phase.
     */
    END_OF_PLACING_PHASE,
    /**
     * A draw action completed and the cards taken are reported.
     */
    CARDS_TAKEN,
    /**
     * An extra draw request was issued to a specific player.
     */
    EXTRA_DRAW_REQUEST,
    /**
     * An extra draw card has been taken.
     */
    EXTRA_CARD_TAKEN,
    /**
     * An event card has been resolved.
     */
    EVENT_RESOLVED,
    /**
     * The era has progressed: the deck and buildings have been refreshed.
     */
    ERA_PROGRESSION,
    /**
     * The card market rows have been refreshed at end of round.
     */
    MARKET_REFRESHED,
    /**
     * The match has ended.
     */
    GAME_OVER,
    /**
     * The final scoreboard is available.
     */
    SCOREBOARD_AVAILABLE,
}
