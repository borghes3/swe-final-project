package it.polimi.ingsw.am23.model.state;

import java.io.Serializable;

/**
 * Immutable snapshot of an offer tile. Carries the tile identifier, the
 * player currently occupying it (if any) and the draw/reward configuration.
 *
 * @param positionIndex      0-based position of the tile on the board
 * @param tileId             letter identifying the tile
 * @param occupiedByPlayerId id of the player currently placed on the tile, or {@code null}
 * @param minPlayers         minimum number of players for which this tile is in play
 * @param topDrawCount       number of cards drawable from the top row
 * @param bottomDrawCount    number of cards drawable from the bottom row
 * @param foodReward         food reward granted upon resolution
 */
public record OfferTileState(int positionIndex, char tileId, String occupiedByPlayerId, int minPlayers,
                             int topDrawCount, int bottomDrawCount, int foodReward) implements Serializable {

    /**
     * Returns the position of this tile on the board.
     *
     * @return the 0-based position of the tile on the board
     */
    @Override
    public int positionIndex() {
        return positionIndex;
    }

    /**
     * Returns the letter that identifies this tile.
     *
     * @return the letter identifying the tile
     */
    @Override
    public char tileId() {
        return tileId;
    }

    /**
     * Returns the id of the player sitting on this tile, or {@code null} when
     * the tile is free.
     *
     * @return id of the player currently placed on the tile, or {@code null}
     */
    @Override
    public String occupiedByPlayerId() {
        return occupiedByPlayerId;
    }

    /**
     * Returns the minimum number of players for which this tile is enabled.
     *
     * @return the minimum number of players for which this tile is in play
     */
    @Override
    public int minPlayers() {
        return minPlayers;
    }

    /**
     * Returns how many cards can be drawn from the top row when this tile is
     * resolved.
     *
     * @return the number of cards drawable from the top row
     */
    @Override
    public int topDrawCount() {
        return topDrawCount;
    }

    /**
     * Returns how many cards can be drawn from the bottom row when this tile
     * is resolved.
     *
     * @return the number of cards drawable from the bottom row
     */
    @Override
    public int bottomDrawCount() {
        return bottomDrawCount;
    }

    /**
     * Returns the food bonus granted to the player when this tile is resolved.
     *
     * @return the food reward granted upon resolution
     */
    @Override
    public int foodReward() {
        return foodReward;
    }
}
