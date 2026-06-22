package it.polimi.ingsw.am23.exceptions;

/**
 * Thrown when a player attempts to draw a card that, due to game rules or
 * the current board state, cannot be drawn from the card market.
 */
public class CardNotDrawableException extends RuntimeException {
    /**
     * Creates a new {@code CardNotDrawableException} with the given detail message.
     *
     * @param message human readable explanation of the cause
     */
    public CardNotDrawableException(String message) {
        super(message);
    }
}
