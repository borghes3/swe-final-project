package it.polimi.ingsw.am23.model.draw;

import it.polimi.ingsw.am23.model.board.CardMarket;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.enums.RowType;

public class CardDrawState {
    private int upperPickedCount;
    private int lowerPickedCount;
    private int upperMaxCount;
    private int lowerMaxCount;
    private boolean drawingStarted;

    public CardDrawState() {
        this.upperPickedCount = 0;
        this.lowerPickedCount = 0;
        this.upperMaxCount = 0;
        this.lowerMaxCount = 0;
        this.drawingStarted = false;
    }

    public void init(OfferTile tile, CardMarket market) {
        this.upperPickedCount = 0;
        this.lowerPickedCount = 0;
        this.upperMaxCount = Math.min(tile.getAction().getUpperDrawRowCount(), market.getDrawableCount(RowType.TOP));
        this.lowerMaxCount = Math.min(tile.getAction().getBottomDrawCount(), market.getDrawableCount(RowType.BOTTOM));
        this.drawingStarted = true;
    }

    public boolean canDraw(SelectedSingleCard card) {
        return switch (card.getRow()) {
            case TOP -> upperPickedCount < upperMaxCount;
            case BOTTOM -> lowerPickedCount < lowerMaxCount;
        };
    }

    public void incrementDrawCount(SelectedSingleCard card) {
        if (card.getRow() == RowType.TOP) {
            upperPickedCount++;
        } else {
            lowerPickedCount++;
        }
    }

    public boolean hasFinishedDrawing() {
        return upperPickedCount == upperMaxCount && lowerPickedCount == lowerMaxCount;
    }

    public void reset() {
        this.upperPickedCount = 0;
        this.lowerPickedCount = 0;
        this.upperMaxCount = 0;
        this.lowerMaxCount = 0;
        this.drawingStarted = false;
    }

    public boolean isDrawingStarted() {
        return drawingStarted;
    }

    public boolean canDrawFromRow(RowType row) {
        return switch (row) {
            case TOP -> upperPickedCount < upperMaxCount;
            case BOTTOM -> lowerPickedCount < lowerMaxCount;
        };
    }
}
