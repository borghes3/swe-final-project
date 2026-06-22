package it.polimi.ingsw.am23.exceptions;

/**
 * Thrown when a lookup on the turn order tile fails because no slot matches
 * the supplied criteria.
 */
public class TurnOrderSlotNotFoundException extends RuntimeException {
    /**
     * Creates a new {@code TurnOrderSlotNotFoundException} with the given detail message.
     *
     * @param message human readable explanation of the cause
     */
    public TurnOrderSlotNotFoundException(String message) {
        super(message);
    }
}
