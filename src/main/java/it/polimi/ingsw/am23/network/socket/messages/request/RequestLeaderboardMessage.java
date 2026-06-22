package it.polimi.ingsw.am23.network.socket.messages.request;

import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class RequestLeaderboardMessage extends Message {

    private final String playerId;
    private final int playerCount;

    public RequestLeaderboardMessage(String playerId, int playerCount) {
        this.playerId = playerId;
        this.playerCount = playerCount;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getPlayerCount() {
        return playerCount;
    }
}
