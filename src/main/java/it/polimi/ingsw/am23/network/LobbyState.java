package it.polimi.ingsw.am23.network;

import it.polimi.ingsw.am23.model.setup.PlayerConnectionInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable snapshot of a lobby. Holds the lobby identity, the owner
 * and the connected players. Shipped to clients to drive the lobby UI.
 */
public class LobbyState implements Serializable {

    private final String lobbyId;
    private final String lobbyName;
    private final String ownerPlayerId;
    private final List<PlayerConnectionInfo> players;
    private final int maxPlayers;
    private LobbyPhase lobbyPhase;

    /**
     * Builds a new lobby state.
     *
     * @param lobbyId       unique identifier of the lobby
     * @param lobbyName     human readable name
     * @param ownerPlayerId id of the player who owns the lobby
     * @param maxPlayers    maximum number of players allowed
     */
    public LobbyState(String lobbyId, String lobbyName, String ownerPlayerId, int maxPlayers) {
        this.lobbyId = lobbyId;
        this.lobbyName = lobbyName;
        this.ownerPlayerId = ownerPlayerId;
        this.maxPlayers = maxPlayers;
        this.players = new ArrayList<>();
        this.lobbyPhase = LobbyPhase.OPEN;
    }

    /**
     * Adds a player to the lobby.
     *
     * @param player the player to add
     */
    public void addPlayer(PlayerConnectionInfo player) {
        players.add(player);
    }

    /**
     * Removes the player identified by {@code playerId}, if present.
     *
     * @param playerId id of the player to remove
     */
    public void removePlayer(String playerId) {
        players.removeIf(p -> p.getId().equals(playerId));
    }

    /** @return {@code true} if the lobby has reached its capacity */
    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    /** @return the unique identifier of the lobby */
    public String getLobbyId() {
        return lobbyId;
    }

    /** @return the human readable lobby name */
    public String getLobbyName() {
        return lobbyName;
    }

    /** @return the id of the player who owns the lobby */
    public String getOwnerPlayerId() {
        return ownerPlayerId;
    }

    /** @return an unmodifiable snapshot of the players currently in the lobby */
    public List<PlayerConnectionInfo> getPlayers() {
        return List.copyOf(players);
    }

    /** @return the maximum number of players allowed in the lobby */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /** @return the number of players currently in the lobby */
    public int getCurrentPlayers() {
        return players.size();
    }

    /**
     * Updates the lobby lifecycle phase.
     *
     * @param lobbyPhase the new phase
     */
    public void setLobbyPhase(LobbyPhase lobbyPhase) {
        this.lobbyPhase = lobbyPhase;
    }

    /** @return the current lifecycle phase */
    public LobbyPhase getLobbyPhase() {
        return lobbyPhase;
    }
}
