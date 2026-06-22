package it.polimi.ingsw.am23.model.draw;

import it.polimi.ingsw.am23.model.enums.RowType;

import java.io.Serializable;

/**
 * DTO that identifies the card a player wants to take during a normal draw
 * action. Carries the row, the index within the row and whether the card
 * is a tribe card or a building.
 */
public record SelectedSingleCard(RowType row, int boardIndex, boolean isBuilding) implements Serializable {
    /**
     * Builds a new draw selection.
     *
     * @param row        row the card belongs to
     * @param boardIndex 0-based index within the row
     * @param isBuilding {@code true} if the selection points to a building, {@code false} for a tribe card
     */
    public SelectedSingleCard {
    }

    /**
     * @return the row the selected card belongs to
     */
    @Override
    public RowType row() {
        return row;
    }

    /**
     * @return the 0-based index of the selected card within its row
     */
    @Override
    public int boardIndex() {
        return boardIndex;
    }

    /**
     * @return {@code true} if the selection points to a building card
     */
    @Override
    public boolean isBuilding() {
        return isBuilding;
    }
}
