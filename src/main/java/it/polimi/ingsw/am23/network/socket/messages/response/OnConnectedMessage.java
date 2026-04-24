package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.socket.messages.Message;

import java.util.List;

public final class OnConnectedMessage extends Message {

    private final String playerId;
    private final List<LobbyState> lobbies;

    public OnConnectedMessage(String playerId, List<LobbyState> lobbies) {
        this.playerId = playerId;
        this.lobbies = lobbies;
    }

    public String getPlayerId() { return playerId; }
    public List<LobbyState> getLobbies() { return lobbies; }
}
