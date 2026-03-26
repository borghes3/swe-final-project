package it.polimi.ingsw.am23.exceptions;

public class NoFreeSlotsException extends RuntimeException {
    public NoFreeSlotsException(String message) {
        super(message);
    }
}
