package it.polimi.ingsw.am23.model;

/**
 * Machine readable error codes carried by {@link ActionResult} when an
 * action is rejected by the {@link GameModel}.
 */
public enum ErrorCode {
    /** No error: the action succeeded. */
    NONE,
    /** The action was issued by a player whose turn is not active. */
    WRONG_PLAYER,
    /** The action is not legal in the current game phase. */
    WRONG_PHASE,
    /** The referenced offer tile is invalid (not found or not free). */
    INVALID_TILE,
    /** The referenced card cannot be selected. */
    INVALID_CARD,
    /** The targeted row cannot accept another draw. */
    INVALID_ROW,
    /** The selected card cannot be taken (e.g. an event card during normal draw). */
    CARD_NOT_TAKABLE,
    /** The player does not have enough food to cover the cost. */
    NOT_ENOUGH_FOOD,
    /** There are no pending extra draws to fulfill. */
    NO_PENDING_EXTRA_DRAW,
    /** The provided extra draw selection is invalid. */
    INVALID_EXTRA_DRAW,
    /** The player attempted to skip when the rules forbid it. */
    CANNOT_SKIP,
    /** The action cannot be performed because the game has already ended. */
    GAME_ENDED,
}
