package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.model.payloads.MarketRefresherPayload;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public class OnMarketRefreshedMessage extends Message {

    private final MarketRefresherPayload payload;

    public OnMarketRefreshedMessage(MarketRefresherPayload payload) {
        this.payload = payload;
    }

    public MarketRefresherPayload getPayload() {
        return payload;
    }
}
