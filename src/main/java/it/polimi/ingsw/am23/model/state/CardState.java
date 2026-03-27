package it.polimi.ingsw.am23.model.state;

import java.util.Objects;

import it.polimi.ingsw.am23.model.enums.CardKind;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;

public final class CardState {

    private final String cardId;
    private final Era era;
    private final CardKind cardKind;
    private final CharacterType characterType;
    private final Integer foodCost;
    private final int printedPoints;


    public CardState(String cardId,
                     Era era,
                     CardKind cardKind,
                     CharacterType characterType,
                     Integer foodCost,
                     int printedPoints) {
        this.cardId = Objects.requireNonNull(cardId, "cardId cannot be null");
        this.era = Objects.requireNonNull(era, "era cannot be null");
        this.cardKind = Objects.requireNonNull(cardKind, "cardKind cannot be null");
        this.characterType = characterType;
        this.foodCost = foodCost;
        this.printedPoints = printedPoints;
    }

    public String getCardId() {
        return cardId;
    }

    public Era getEra() {
        return era;
    }

    public CardKind getCardKind() {
        return cardKind;
    }

    public CharacterType getCharacterType() {
        return characterType;
    }

    public Integer getFoodCost() {
        return foodCost;
    }

    public int getPrintedPoints() {
        return printedPoints;
    }

}
