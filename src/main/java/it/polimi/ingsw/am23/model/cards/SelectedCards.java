package it.polimi.ingsw.am23.model.cards;

import java.util.List;

public class SelectedCards {
    private List<Integer> upperRowIndexes;
    private List<Integer> lowerRowIndexes;

    public SelectedCards(List<Integer> upperRowIndexes, List<Integer> lowerRowIndexes) {
        this.upperRowIndexes = upperRowIndexes;
        this.lowerRowIndexes = lowerRowIndexes;
    }
}
