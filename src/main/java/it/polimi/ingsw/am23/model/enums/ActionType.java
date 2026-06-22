package it.polimi.ingsw.am23.model.enums;

/**
 * Identifies the kind of player action that produced an {@link
 * it.polimi.ingsw.am23.model.ActionResult}.
 */
public enum ActionType {
    /**
     * A totem placement onto an offer tile.
     */
    PLACE_TOTEM,
    /**
     * Drawing a character or event card.
     */
    TAKE_CARD,
    /**
     * Drawing a building card.
     */
    TAKE_BUILDING,
    /**
     * Skipping the current player's draw turn.
     */
    SKIP_TURN,
    /**
     * Ending the round, triggering event resolution.
     */
    END_ROUND,
    /**
     * Generic action that does not fit the more specific categories.
     */
    GENERIC
}
