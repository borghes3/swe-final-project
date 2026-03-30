package it.polimi.ingsw.am23.model.cards;

import java.util.List;

public class SelectedCards {
    private final List<Integer> upperRow;
    private final List<Integer> lowerRow;
    private final List<Integer> upperBuildings;
    private final List<Integer> lowerBuildings;

    public SelectedCards(List<Integer> upperRow, List<Integer> lowerRow, List<Integer> upperBuildings, List<Integer> lowerBuildings) {
        this.upperRow = upperRow;
        this.lowerRow = lowerRow;
        this.upperBuildings = upperBuildings;
        this.lowerBuildings = lowerBuildings;
    }

    public List<Integer> getUpperRow() {
        return upperRow;
    }

    public List<Integer> getLowerRow() {
        return lowerRow;
    }

    public List<Integer> getUpperBuildings() {
        return upperBuildings;
    }

    public List<Integer> getLowerBuildings() {
        return lowerBuildings;
    }
}
