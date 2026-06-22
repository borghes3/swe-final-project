package it.polimi.ingsw.am23.model.enums;

/**
 * Lifecycle phases of a match, traversed sequentially by the model.
 */
public enum GamePhase {
    /**
     * Initial configuration phase, before {@code startGame} is called.
     */
    SETUP,
    /**
     * Players place their totems on the offer tiles.
     */
    PLACING_TOTEMS,
    /**
     * Offer tiles are resolved in order and players draw cards.
     */
    RESOLVING_OFFERS,
    /**
     * Events triggered at the end of the round are resolved.
     */
    RESOLVING_EVENTS,
    /**
     * A player with the extra draw effect picks an extra card.
     */
    EXTRA_DRAW,
    /**
     * The match has reached its terminal state.
     */
    ENDED
}
