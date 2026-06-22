package it.polimi.ingsw.am23.model.enums;

/**
 * High level classification of the cards used by the model.
 */
public enum CardKind {
    /** A character card (Hunter, Gatherer, etc.). */
    CHARACTER,
    /** An event card that triggers a scoring step during the event phase. */
    EVENT,
    /** A building card that can be purchased and grants a persistent effect. */
    BUILDING
}
