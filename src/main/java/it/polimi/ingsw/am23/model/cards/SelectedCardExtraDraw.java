package it.polimi.ingsw.am23.model.cards;

public class SelectedCardExtraDraw {
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