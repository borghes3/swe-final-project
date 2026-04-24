package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class OnJoinErrorMessage extends Message {

    private final String reason;

    public OnJoinErrorMessage(String reason) {
        this.reason = reason;
    }

    public String getReason() { return reason; }
}
