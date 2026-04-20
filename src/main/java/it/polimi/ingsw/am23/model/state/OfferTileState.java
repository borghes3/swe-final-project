package it.polimi.ingsw.am23.model.state;

import java.io.Serializable;

public final class OfferTileState implements Serializable {
    private final int positionIndex;
    private final char tileId;
    private final String occupiedByPlayerId;
    private final int minPlayers;
    private final int topDrawCount;
    private final int bottomDrawCount;
    private final int foodReward;

    public OfferTileState(int positionIndex,
                          char tileId,
                          String occupiedByPlayerId,
                          int minPlayers,
                          int topDrawCount,
                          int bottomDrawCount,
                          int foodReward) {
        this.positionIndex = positionIndex;
        this.tileId = tileId;
        this.occupiedByPlayerId = occupiedByPlayerId;
        this.minPlayers = minPlayers;
        this.topDrawCount = topDrawCount;
        this.bottomDrawCount = bottomDrawCount;
        this.foodReward = foodReward;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public char getTileId() {
        return tileId;
    }

    public String getOccupiedByPlayerId() {
        return occupiedByPlayerId;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getTopDrawCount() {
        return topDrawCount;
    }

    public int getBottomDrawCount() {
        return bottomDrawCount;
    }

    public int getFoodReward() {
        return foodReward;
    }
}
