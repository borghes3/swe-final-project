package it.polimi.ingsw.am23.network.socket.messages.request;

import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class LeaveLobbyMessage extends Message {

    private final String playerId;
    private final String lobbyId;

    public LeaveLobbyMessage(String playerId, String lobbyId) {
        this.playerId = playerId;
        this.lobbyId = lobbyId;
    }

    public String getPlayerId() { return playerId; }
    public String getLobbyId() { return lobbyId; }
}
