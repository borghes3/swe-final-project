package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.socket.messages.Message;

import java.util.List;

public final class OnLobbyListUpdatedMessage extends Message {

    private final List<LobbyState> lobbies;

    public OnLobbyListUpdatedMessage(List<LobbyState> lobbies) {
        this.lobbies = lobbies;
    }

    public List<LobbyState> getLobbies() {
        return lobbies;
    }
}
