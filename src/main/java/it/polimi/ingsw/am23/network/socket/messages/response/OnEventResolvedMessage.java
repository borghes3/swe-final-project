package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.model.payloads.EventResolvedPayload;
import it.polimi.ingsw.am23.model.state.GameState;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class OnEventResolvedMessage extends Message {

    private final EventResolvedPayload payload;

    public OnEventResolvedMessage(EventResolvedPayload payload) {
        this.payload = payload;
    }

    public EventResolvedPayload getPayload() { return payload; }
}

