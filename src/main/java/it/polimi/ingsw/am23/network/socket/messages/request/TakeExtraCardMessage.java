package it.polimi.ingsw.am23.network.socket.messages.request;

import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class TakeExtraCardMessage extends Message {

    private final String playerId;
    private final int index;

    public TakeExtraCardMessage(String playerId, int index) {
        this.playerId = playerId;
        this.index = index;
    }

    public String getPlayerId() { return playerId; }
    public int getIndex() { return index; }
}
