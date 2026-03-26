package it.polimi.ingsw.am23.exceptions;

public class NoPlayersInTurnOrderException extends RuntimeException {
    public NoPlayersInTurnOrderException(String message) {
        super(message);
    }
}
