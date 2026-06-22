package it.polimi.ingsw.am23.exceptions;

/**
 * Thrown when an operation requires a free slot on a board structure
 * (e.g. the turn order tile) but none is available.
 */
public class NoFreeSlotsException extends RuntimeException {
    /**
     * Creates a new {@code NoFreeSlotsException} with the given detail message.
     *
     * @param message human readable explanation of the cause
     */
    public NoFreeSlotsException(String message) {
        super(message);
    }
}
