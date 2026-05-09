package it.polimi.ingsw.am23.network;

import it.polimi.ingsw.am23.model.setup.PlayerConnectionInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LobbyState implements Serializable {

    private final String lobbyId;
    private final String lobbyName;
    private final String ownerPlayerId;
    private final List<PlayerConnectionInfo> players;
    private final int maxPlayers;
    private LobbyPhase lobbyPhase;

    public LobbyState(String lobbyId, String lobbyName, String ownerPlayerId, int maxPlayers) {
        this.lobbyId = lobbyId;
        this.lobbyName = lobbyName;
        this.ownerPlayerId = ownerPlayerId;
        this.maxPlayers = maxPlayers;
        this.players = new ArrayList<>();
        this.lobbyPhase = LobbyPhase.OPEN;
    }

    public void addPlayer(PlayerConnectionInfo player) {
        players.add(player);
    }

    public void removePlayer(String playerId) {
        players.removeIf(p -> p.getId().equals(playerId));
    }

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public String getOwnerPlayerId() {
        return ownerPlayerId;
    }

    public List<PlayerConnectionInfo> getPlayers() {
        return List.copyOf(players);
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getCurrentPlayers() {
        return players.size();
    }

    public void setLobbyPhase(LobbyPhase lobbyPhase) {
        this.lobbyPhase = lobbyPhase;
    }

    public LobbyPhase getLobbyPhase() {
        return lobbyPhase;
    }
}
