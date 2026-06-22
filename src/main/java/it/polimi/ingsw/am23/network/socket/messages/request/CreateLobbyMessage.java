package it.polimi.ingsw.am23.network.socket.messages.request;

import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class CreateLobbyMessage extends Message {

    private final String playerId;
    private final String lobbyName;
    private final int maxPlayers;

    public CreateLobbyMessage(String playerId, String lobbyName, int maxPlayers) {
        this.playerId = playerId;
        this.lobbyName = lobbyName;
        this.maxPlayers = maxPlayers;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }
}
