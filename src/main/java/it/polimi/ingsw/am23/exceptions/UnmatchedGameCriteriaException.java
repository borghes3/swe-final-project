package it.polimi.ingsw.am23.exceptions;

/**
 * Thrown when the requested game configuration does not match any of the
 * supported game criteria (e.g. unsupported player count).
 */
public class UnmatchedGameCriteriaException extends RuntimeException {
    /**
     * Creates a new {@code UnmatchedGameCriteriaException} with the given detail message.
     *
     * @param message human readable explanation of the cause
     */
    public UnmatchedGameCriteriaException(String message) {
        super(message);
    }
}
