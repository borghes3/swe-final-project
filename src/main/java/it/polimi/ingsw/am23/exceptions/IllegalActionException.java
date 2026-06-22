package it.polimi.ingsw.am23.exceptions;

/**
 * Thrown when a player attempts to perform an action that is not allowed in
 * the current game phase or with the current state of the model.
 */
public class IllegalActionException extends RuntimeException {
    /**
     * Creates a new {@code IllegalActionException} with the given detail message.
     *
     * @param message human readable explanation of the cause
     */
    public IllegalActionException(String message) {
        super(message);
    }
}
