package it.polimi.ingsw.am23.model.player;

import java.util.Objects;

/**
 * The pawn placed by a player on the offer track and on the turn order
 * tile to claim an action. Carries the owning player's id and the hex
 * color used by the renderer.
 */
public class Totem {
    private final String ownerId;
    private final String color;

    /**
     * Builds a new totem.
     *
     * @param ownerId id of the player that owns this totem
     * @param color   hex color associated to the totem
     */
    public Totem(String ownerId, String color) {
        this.ownerId = Objects.requireNonNull(ownerId);
        this.color = color;
    }

    /** @return id of the player that owns this totem */
    public String getOwnerId() {
        return ownerId;
    }

    /** @return the hex color string of this totem */
    public String getColor() {
        return color;
    }
}
