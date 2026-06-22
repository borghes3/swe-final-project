package it.polimi.ingsw.am23.model.draw;

import java.io.Serializable;

/**
 * DTO that identifies the card a player wants to take during their extra
 * draw turn. Exactly one of {@code cardIndex} and {@code buildingIndex}
 * must be set; the other is {@code null}.
 */
public class SelectedCardExtraDraw implements Serializable {
    private final Integer cardIndex;
    private final Integer buildingIndex;

    /**
     * Builds a new extra draw selection.
     *
     * @param cardIndex     index of the tribe card to take, or {@code null}
     * @param buildingIndex index of the building to take, or {@code null}
     */
    public SelectedCardExtraDraw(Integer cardIndex, Integer buildingIndex) {
        this.cardIndex = cardIndex;
        this.buildingIndex = buildingIndex;
    }

    /**
     * @return the index of the building to take
     */
    public int getBuildingIndex() {
        return buildingIndex;
    }

    /**
     * @return the index of the tribe card to take
     */
    public int getCardIndex() {
        return cardIndex;
    }

    /**
     * @return {@code true} if the selection points to a tribe card
     */
    public boolean isTribeCard() {
        return cardIndex != null;
    }
}
