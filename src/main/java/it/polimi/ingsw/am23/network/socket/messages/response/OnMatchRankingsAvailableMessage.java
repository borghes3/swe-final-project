package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.model.payloads.MatchRankingsPayload;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class OnMatchRankingsAvailableMessage extends Message {

    private final MatchRankingsPayload payload;

    public OnMatchRankingsAvailableMessage(MatchRankingsPayload payload) {
        this.payload = payload;
    }

    public MatchRankingsPayload getPayload() {
        return payload;
    }
}
