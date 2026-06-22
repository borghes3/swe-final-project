package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.model.payloads.EraProgressionPayload;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class OnEraProgressionMessage extends Message {

    private final EraProgressionPayload payload;

    public OnEraProgressionMessage(EraProgressionPayload payload) {
        this.payload = payload;
    }

    public EraProgressionPayload getPayload() {
        return payload;
    }
}
