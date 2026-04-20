package it.polimi.ingsw.am23.model.state;

import java.io.Serializable;

public final class TurnOrderSlotState implements Serializable {
    private final int positionIndex;
    private final int foodDelta;
    private final String occupiedByPlayerId;

    public TurnOrderSlotState(int positionIndex, int foodDelta, String occupiedByPlayerId) {
        this.positionIndex = positionIndex;
        this.foodDelta = foodDelta;
        this.occupiedByPlayerId = occupiedByPlayerId;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public int getFoodDelta() {
        return foodDelta;
    }

    public String getOccupiedByPlayerId() {
        return occupiedByPlayerId;
    }
}