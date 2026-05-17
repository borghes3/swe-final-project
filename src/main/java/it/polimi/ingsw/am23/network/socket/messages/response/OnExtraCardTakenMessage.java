package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.model.payloads.ExtraCardTakenPayload;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public class OnExtraCardTakenMessage extends Message {

    private final ExtraCardTakenPayload payload;

    public OnExtraCardTakenMessage(ExtraCardTakenPayload payload) {
        this.payload = payload;
    }

    public ExtraCardTakenPayload getPayload() { return payload; }
}
