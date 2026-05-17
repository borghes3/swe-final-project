package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.model.payloads.TotemPlacedPayload;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public class OnTotemPlacedMessage extends Message {

    private final TotemPlacedPayload payload;

    public OnTotemPlacedMessage(TotemPlacedPayload payload) {
        this.payload = payload;
    }

    public TotemPlacedPayload getPayload() { return payload; }
}