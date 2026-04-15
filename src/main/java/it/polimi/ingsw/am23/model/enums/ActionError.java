package it.polimi.ingsw.am23.model.enums;

public enum ActionError {
    NONE,
    WRONG_PHASE,
    NOT_YOUR_TURN,
    INVALID_TILE, // not found
    TILE_ALREADY_OCCUPIED,
    INVALID_CARD_SELECTION,
    CARD_NOT_TAKABLE,
    NOT_ENOUGH_FOOD,
    INVALID_ACTION,
    GAME_ALREADY_STARTED,
    GAME_NOT_STARTED
}
