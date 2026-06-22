package it.polimi.ingsw.am23.model.player;

import java.util.Objects;

/**
 * The pawn placed by a player on the offer track and on the turn order
 * tile to claim an action. Carries the owning player's id and the hex
 * color used by the renderer.
 *
 * @param ownerId id of the player that owns this totem
 * @param color   hex color associated to the totem
 */
public record Totem(String ownerId, String color) {
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

    /**
     * Returns the hex color string used to render this totem.
     *
     * @return the hex color string of this totem
     */
    @Override
    public String color() {
        return color;
    }
}
