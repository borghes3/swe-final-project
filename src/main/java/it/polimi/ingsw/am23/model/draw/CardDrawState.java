package it.polimi.ingsw.am23.model.draw;

import it.polimi.ingsw.am23.model.board.CardMarket;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.enums.RowType;

/**
 * Per-turn draw state used by {@link it.polimi.ingsw.am23.model.Game} to
 * track how many cards a player has already drawn from each row during the
 * resolution of an offer tile.
 */
public class CardDrawState {
    private int upperPickedCount;
    private int lowerPickedCount;
    private int upperMaxCount;
    private int lowerMaxCount;
    private boolean drawingStarted;

    /** Builds a new, idle draw state. */
    public CardDrawState() {
        this.upperPickedCount = 0;
        this.lowerPickedCount = 0;
        this.upperMaxCount = 0;
        this.lowerMaxCount = 0;
        this.drawingStarted = false;
    }

    /**
     * Initializes the state at the beginning of a draw turn, using the tile
     * allowance clamped to the cards actually available in the market.
     *
     * @param tile   the offer tile being resolved
     * @param market the card market the player is drawing from
     */
    public void init(OfferTile tile, CardMarket market) {
        this.upperPickedCount = 0;
        this.lowerPickedCount = 0;
        this.upperMaxCount = Math.min(tile.getAction().getUpperDrawRowCount(), market.getDrawableCount(RowType.TOP));
        this.lowerMaxCount = Math.min(tile.getAction().getBottomDrawCount(), market.getDrawableCount(RowType.BOTTOM));
        this.drawingStarted = true;
    }

    /**
     * Tells whether the player can still draw the supplied card given the
     * remaining allowance for that row.
     *
     * @param card the card the player wants to draw
     * @return {@code true} if the row still has remaining draws
     */
    public boolean canDraw(SelectedSingleCard card) {
        return switch (card.getRow()) {
            case TOP -> upperPickedCount < upperMaxCount;
            case BOTTOM -> lowerPickedCount < lowerMaxCount;
        };
    }

    /**
     * Increments the counter for the row the supplied card was picked from.
     *
     * @param card the card just drawn
     */
    public void incrementDrawCount(SelectedSingleCard card) {
        if (card.getRow() == RowType.TOP) {
            upperPickedCount++;
        } else {
            lowerPickedCount++;
        }
    }

    /** @return {@code true} if the player has exhausted both rows' allowances */
    public boolean hasFinishedDrawing() {
        return upperPickedCount == upperMaxCount && lowerPickedCount == lowerMaxCount;
    }

    /** Resets the state at the end of a turn. */
    public void reset() {
        this.upperPickedCount = 0;
        this.lowerPickedCount = 0;
        this.upperMaxCount = 0;
        this.lowerMaxCount = 0;
        this.drawingStarted = false;
    }

    /** @return {@code true} if a draw turn is currently in progress */
    public boolean isDrawingStarted() {
        return drawingStarted;
    }

    /**
     * Tells whether the player can still draw from the supplied row.
     *
     * @param row row to inspect
     * @return {@code true} if the row still has remaining draws
     */
    public boolean canDrawFromRow(RowType row) {
        return switch (row) {
            case TOP -> upperPickedCount < upperMaxCount;
            case BOTTOM -> lowerPickedCount < lowerMaxCount;
        };
    }
}
