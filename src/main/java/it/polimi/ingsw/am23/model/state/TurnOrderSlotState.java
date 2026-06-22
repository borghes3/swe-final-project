package it.polimi.ingsw.am23.model.state;

import java.io.Serializable;

/**
 * Immutable snapshot of a single turn order slot, including the resource
 * delta granted by the slot and the player currently occupying it.
 *
 * @param positionIndex      0-based position of the slot on the turn order tile
 * @param foodDelta          food delta granted (positive) or charged (negative) by the slot
 * @param occupiedByPlayerId id of the player on the slot, or {@code null}
 */
public record TurnOrderSlotState(int positionIndex, int foodDelta, String occupiedByPlayerId) implements Serializable {

    /**
     * Returns the position of this slot on the turn order tile.
     *
     * @return the 0-based position of the slot on the turn order tile
     */
    @Override
    public int positionIndex() {
        return positionIndex;
    }

    /**
     * Returns the food change this slot applies to its player. Positive values
     * grant food, negative values charge it.
     *
     * @return the food delta granted (positive) or charged (negative) by the slot
     */
    @Override
    public int foodDelta() {
        return foodDelta;
    }

    /**
     * Returns the id of the player sitting on this slot, or {@code null} when
     * the slot is free.
     *
     * @return id of the player currently on the slot, or {@code null}
     */
    @Override
    public String occupiedByPlayerId() {
        return occupiedByPlayerId;
    }
}
