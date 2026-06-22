package it.polimi.ingsw.am23.model.setup;

import java.io.Serializable;

/**
 * Minimal serializable descriptor of a connected player, used during the
 * lobby and setup phases before the full {@link
 * it.polimi.ingsw.am23.model.player.Player} model is instantiated.
 */
public class PlayerConnectionInfo implements Serializable {
    private String id;
    private String nickname;

    /**
     * Builds a new connection info entry.
     *
     * @param id       unique identifier of the player
     * @param nickname display nickname of the player
     */
    public PlayerConnectionInfo(String id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    /** @return the unique identifier of the player */
    public String getId() {
        return id;
    }

    /** @return the display nickname of the player */
    public String getNickname() {
        return nickname;
    }
}
