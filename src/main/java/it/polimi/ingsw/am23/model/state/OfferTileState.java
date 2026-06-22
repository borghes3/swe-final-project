package it.polimi.ingsw.am23.model.state;

import java.io.Serializable;

/**
 * Immutable snapshot of an offer tile. Carries the tile identifier, the
 * player currently occupying it (if any) and the draw/reward configuration.
 */
public final class OfferTileState implements Serializable {
    private final int positionIndex;
    private final char tileId;
    private final String occupiedByPlayerId;
    private final int minPlayers;
    private final int topDrawCount;
    private final int bottomDrawCount;
    private final int foodReward;

    /**
     * Builds a new offer tile snapshot.
     *
     * @param positionIndex      0-based position of the tile on the board
     * @param tileId             letter identifying the tile
     * @param occupiedByPlayerId id of the player currently placed on the tile, or {@code null}
     * @param minPlayers         minimum number of players for which this tile is in play
     * @param topDrawCount       number of cards drawable from the top row
     * @param bottomDrawCount    number of cards drawable from the bottom row
     * @param foodReward         food reward granted upon resolution
     */
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

    /** @return the 0-based position of the tile on the board */
    public int getPositionIndex() {
        return positionIndex;
    }

    /** @return the letter identifying the tile */
    public char getTileId() {
        return tileId;
    }

    /** @return id of the player currently placed on the tile, or {@code null} */
    public String getOccupiedByPlayerId() {
        return occupiedByPlayerId;
    }

    /** @return the minimum number of players for which this tile is in play */
    public int getMinPlayers() {
        return minPlayers;
    }

    /** @return the number of cards drawable from the top row */
    public int getTopDrawCount() {
        return topDrawCount;
    }

    /** @return the number of cards drawable from the bottom row */
    public int getBottomDrawCount() {
        return bottomDrawCount;
    }

    /** @return the food reward granted upon resolution */
    public int getFoodReward() {
        return foodReward;
    }
}
