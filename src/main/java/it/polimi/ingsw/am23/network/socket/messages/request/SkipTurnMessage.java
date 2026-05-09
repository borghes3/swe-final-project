package it.polimi.ingsw.am23.network.socket.messages.request;

import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class SkipTurnMessage extends Message {

    private final String playerId;

    public SkipTurnMessage(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerId() { return playerId; }
}
