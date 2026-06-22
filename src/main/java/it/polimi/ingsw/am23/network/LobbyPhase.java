package it.polimi.ingsw.am23.network;

/**
 * Lifecycle status of a lobby.
 */
public enum LobbyPhase {
    /**
     * The lobby is open and accepts new players.
     */
    OPEN,
    /**
     * The lobby is closed (e.g. match started); it no longer accepts new players.
     */
    CLOSE
}
