package it.polimi.ingsw.am23.model.draw;

import it.polimi.ingsw.am23.model.enums.RowType;

import java.io.Serializable;

/**
 * DTO that identifies the card a player wants to take during a normal draw
 * action. Carries the row, the index within the row and whether the card
 * is a tribe card or a building.
 */
public class SelectedSingleCard implements Serializable {
    private final RowType row;
    private final int boardIndex;
    private final boolean isBuilding;


    /**
     * Builds a new draw selection.
     *
     * @param row        row the card belongs to
     * @param boardIndex 0-based index within the row
     * @param isBuilding {@code true} if the selection points to a building, {@code false} for a tribe card
     */
    public SelectedSingleCard(RowType row, int boardIndex, boolean isBuilding) {
        this.row = row;
        this.boardIndex = boardIndex;
        this.isBuilding = isBuilding;
    }

    /** @return the row the selected card belongs to */
    public RowType getRow() { return row; }

    /** @return the 0-based index of the selected card within its row */
    public int getBoardIndex() { return boardIndex; }

    /** @return {@code true} if the selection points to a building card */
    public boolean isBuilding() { return isBuilding; }
}
