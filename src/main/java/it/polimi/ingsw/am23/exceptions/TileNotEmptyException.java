package it.polimi.ingsw.am23.exceptions;

/**
 * Thrown when a placement operation is attempted on a tile that is already
 * occupied by another totem or piece.
 */
public class TileNotEmptyException extends RuntimeException {
    /**
     * Creates a new {@code TileNotEmptyException} with the given detail message.
     *
     * @param message human readable explanation of the cause
     */
    public TileNotEmptyException(String message) {
        super(message);
    }
}
