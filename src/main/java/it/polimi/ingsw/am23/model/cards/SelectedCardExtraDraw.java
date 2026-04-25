package it.polimi.ingsw.am23.model.cards;

import java.io.Serializable;

public class SelectedCardExtraDraw implements Serializable {
    private final Integer cardIndex;
    private final Integer buildingIndex;

    public SelectedCardExtraDraw(Integer cardIndex, Integer buildingIndex) {
        this.cardIndex = cardIndex;
        this.buildingIndex = buildingIndex;
    }

    public int getBuildingIndex() {
        return buildingIndex;
    }

    public int getCardIndex() {
        return cardIndex;
    }

    public boolean isTribeCard() {
        return cardIndex != null;
    }
}