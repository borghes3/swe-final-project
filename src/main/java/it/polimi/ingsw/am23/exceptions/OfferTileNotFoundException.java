package it.polimi.ingsw.am23.exceptions;

/**
 * Thrown when a lookup for an offer tile fails because no tile matches the
 * provided identifier on the current board.
 */
public class OfferTileNotFoundException extends RuntimeException {
    /**
     * Creates a new {@code OfferTileNotFoundException} with the given detail message.
     *
     * @param message human readable explanation of the cause
     */
    public OfferTileNotFoundException(String message) {
        super(message);
    }
}
