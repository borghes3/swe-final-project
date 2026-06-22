package it.polimi.ingsw.am23.exceptions;

/**
 * Thrown when a player lookup fails because the supplied identifier does not
 * match any player currently registered in the match.
 */
public class PlayerNotFoundException extends RuntimeException {
    /**
     * Creates a new {@code PlayerNotFoundException} with the given detail message.
     *
     * @param message human readable explanation of the cause
     */
    public PlayerNotFoundException(String message) {
        super(message);
    }
}
