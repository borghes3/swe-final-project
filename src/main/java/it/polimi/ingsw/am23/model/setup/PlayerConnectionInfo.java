package it.polimi.ingsw.am23.model.setup;

import it.polimi.ingsw.am23.model.player.Player;

import java.io.Serializable;

/**
 * Minimal serializable descriptor of a connected player, used during the
 * lobby and setup phases before the full {@link
 * Player} model is instantiated.
 *
 * @param id       unique identifier of the player
 * @param nickname display nickname of the player
 */
public record PlayerConnectionInfo(String id, String nickname) implements Serializable {

    /**
     * Returns the unique id of this player.
     *
     * @return the unique identifier of the player
     */
    @Override
    public String id() {
        return id;
    }

    /**
     * Returns the nickname shown for this player.
     *
     * @return the display nickname of the player
     */
    @Override
    public String nickname() {
        return nickname;
    }
}
