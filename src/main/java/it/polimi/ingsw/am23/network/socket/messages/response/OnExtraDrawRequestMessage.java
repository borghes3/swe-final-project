package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.model.payloads.ExtraDrawRequestPayload;
import it.polimi.ingsw.am23.model.state.GameState;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class OnExtraDrawRequestMessage extends Message {

    private final ExtraDrawRequestPayload payload;

    public OnExtraDrawRequestMessage(ExtraDrawRequestPayload payload) {
        this.payload = payload;
    }

    public ExtraDrawRequestPayload getPayload() { return payload; }
}
