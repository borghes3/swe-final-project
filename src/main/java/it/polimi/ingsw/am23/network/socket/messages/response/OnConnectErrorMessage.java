package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class OnConnectErrorMessage extends Message {

    private final String reason;

    public OnConnectErrorMessage(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
