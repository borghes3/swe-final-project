package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class OnLobbyCreatedMessage extends Message {

    private final LobbyState lobby;

    public OnLobbyCreatedMessage(LobbyState lobby) {
        this.lobby = lobby;
    }

    public LobbyState getLobby() { return lobby; }
}
