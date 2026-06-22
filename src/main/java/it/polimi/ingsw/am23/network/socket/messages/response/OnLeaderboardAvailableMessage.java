package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.model.payloads.LeaderboardPayload;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class OnLeaderboardAvailableMessage extends Message {

    private final LeaderboardPayload payload;

    public OnLeaderboardAvailableMessage(LeaderboardPayload payload) {
        this.payload = payload;
    }

    public LeaderboardPayload getPayload() {
        return payload;
    }
}
