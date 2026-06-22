package it.polimi.ingsw.am23.model.enums;

/**
 * Enumerates the error categories that may be returned to a client when a
 * requested player action is rejected by the model.
 */
public enum ActionError {
    /**
     * No error: the action succeeded.
     */
    NONE,
    /**
     * The action is not legal in the current game phase.
     */
    WRONG_PHASE,
    /**
     * The action was issued by a player whose turn is not active.
     */
    NOT_YOUR_TURN,
    /**
     * The referenced tile does not exist on the current board.
     */
    INVALID_TILE,
    /**
     * The referenced tile is already occupied by another totem.
     */
    TILE_ALREADY_OCCUPIED,
    /**
     * The selected card cannot be picked given the current draw state.
     */
    INVALID_CARD_SELECTION,
    /**
     * The selected card cannot be taken (e.g. event card during normal draw).
     */
    CARD_NOT_TAKABLE,
    /**
     * The player does not have enough food to cover the required cost.
     */
    NOT_ENOUGH_FOOD,
    /**
     * Catch-all for actions that are otherwise invalid.
     */
    INVALID_ACTION,
    /**
     * The game has already been started and cannot be started again.
     */
    GAME_ALREADY_STARTED,
    /**
     * The game has not been started yet, so the action is not allowed.
     */
    GAME_NOT_STARTED
}
