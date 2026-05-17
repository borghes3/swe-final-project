package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.model.payloads.ScoreBoardPayload;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class OnScoreboardAvailableMessage extends Message {

    private final ScoreBoardPayload payload;

    public OnScoreboardAvailableMessage(ScoreBoardPayload payload) {
        this.payload = payload;
    }

    public ScoreBoardPayload getPayload() { return payload; }
}
