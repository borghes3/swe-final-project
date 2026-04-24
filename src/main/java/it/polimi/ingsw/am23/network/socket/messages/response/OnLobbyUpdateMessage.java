package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class OnLobbyUpdateMessage extends Message {

    private final LobbyState lobby;

    public OnLobbyUpdateMessage(LobbyState lobby) {
        this.lobby = lobby;
    }

    public LobbyState getLobby() { return lobby; }
}
