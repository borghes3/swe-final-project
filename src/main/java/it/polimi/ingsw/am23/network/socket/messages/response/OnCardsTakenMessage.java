package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.model.payloads.CardsTakenPayload;
import it.polimi.ingsw.am23.model.state.GameState;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class OnCardsTakenMessage extends Message {

    private final CardsTakenPayload payload;

    public OnCardsTakenMessage(CardsTakenPayload payload) {
        this.payload = payload;
    }

    public CardsTakenPayload getPayload() { return payload; }
}

