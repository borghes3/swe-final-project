package it.polimi.ingsw.am23.model.state;

import java.io.Serializable;

/**
 * Immutable snapshot of a single turn order slot, including the resource
 * delta granted by the slot and the player currently occupying it.
 */
public final class TurnOrderSlotState implements Serializable {
    private final int positionIndex;
    private final int foodDelta;
    private final String occupiedByPlayerId;

    /**
     * Builds a new turn order slot snapshot.
     *
     * @param positionIndex      0-based position of the slot on the turn order tile
     * @param foodDelta          food delta granted (positive) or charged (negative) by the slot
     * @param occupiedByPlayerId id of the player on the slot, or {@code null}
     */
    public TurnOrderSlotState(int positionIndex, int foodDelta, String occupiedByPlayerId) {
        this.positionIndex = positionIndex;
        this.foodDelta = foodDelta;
        this.occupiedByPlayerId = occupiedByPlayerId;
    }

    /** @return the 0-based position of the slot on the turn order tile */
    public int getPositionIndex() {
        return positionIndex;
    }

    /** @return the food delta granted (positive) or charged (negative) by the slot */
    public int getFoodDelta() {
        return foodDelta;
    }

    /** @return id of the player currently on the slot, or {@code null} */
    public String getOccupiedByPlayerId() {
        return occupiedByPlayerId;
    }
}
