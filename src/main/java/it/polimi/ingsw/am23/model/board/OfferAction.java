package it.polimi.ingsw.am23.model.board;

/**
 * Immutable description of the draws and the food reward granted by an
 * {@link OfferTile}. Captures how many cards can be picked from each row of
 * the card market and the food rebate granted when the tile is resolved.
 */
public class OfferAction {

    private final int topDrawCount;
    private final int bottomDrawCount;
    private final int foodReward;

    /**
     * Builds a new offer action description.
     *
     * @param upperRowCount number of cards drawable from the top row
     * @param lowerRowCount number of cards drawable from the bottom row
     * @param foodReward    food reward granted upon resolution
     */
    public OfferAction(int upperRowCount, int lowerRowCount, int foodReward) {
        this.topDrawCount = upperRowCount;
        this.bottomDrawCount = lowerRowCount;
        this.foodReward = foodReward;
    }

    /** @return the number of cards drawable from the top row */
    public int getUpperDrawRowCount() {
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
