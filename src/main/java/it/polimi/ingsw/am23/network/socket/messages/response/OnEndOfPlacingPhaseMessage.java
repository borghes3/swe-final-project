package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.model.payloads.EndOfPlacingPhasePayload;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class OnEndOfPlacingPhaseMessage extends Message {

    private final EndOfPlacingPhasePayload payload;

    public OnEndOfPlacingPhaseMessage(EndOfPlacingPhasePayload payload) {
        this.payload = payload;
    }

    public EndOfPlacingPhasePayload getPayload() {
        return payload;
    }
}
