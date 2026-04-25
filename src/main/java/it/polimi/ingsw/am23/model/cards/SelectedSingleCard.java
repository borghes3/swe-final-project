package it.polimi.ingsw.am23.model.cards;

import it.polimi.ingsw.am23.model.enums.RowType;

import java.io.Serializable;
import java.util.List;

public class SelectedSingleCard implements Serializable {
    private final RowType row;
    private final int boardIndex;
    private final boolean isBuilding;


    public SelectedSingleCard(RowType row, int boardIndex, boolean isBuilding) {
        this.row = row;
        this.boardIndex = boardIndex;
        this.isBuilding = isBuilding;
    }

    public RowType getRow() { return row; }

    public int getBoardIndex() { return boardIndex; }

    public boolean isBuilding() { return isBuilding; }
}
