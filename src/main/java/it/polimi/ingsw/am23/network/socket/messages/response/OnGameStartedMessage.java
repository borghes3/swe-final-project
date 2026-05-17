package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.model.payloads.GameStartedPayload;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class OnGameStartedMessage extends Message {

    private final GameStartedPayload payload;

    public OnGameStartedMessage(GameStartedPayload payload) {
        this.payload = payload;
    }

    public GameStartedPayload getPayload() { return payload; }
}
