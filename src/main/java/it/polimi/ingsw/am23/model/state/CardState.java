package it.polimi.ingsw.am23.model.state;

import it.polimi.ingsw.am23.model.enums.CardKind;
import it.polimi.ingsw.am23.model.enums.Era;

import java.io.Serializable;
import java.util.Objects;

public abstract class CardState implements Serializable {

    private final String cardId;
    private final Era era;
    private final int printedPoints;


    protected CardState(String cardId,
                     Era era,
                     int printedPoints){
        this.cardId = Objects.requireNonNull(cardId, "cardId cannot be null");
        this.era = Objects.requireNonNull(era, "era cannot be null");
        this.printedPoints = printedPoints;
    }

    public String getCardId() {
        return cardId;
    }

    public Era getEra() {
        return era;
    }

    public abstract CardKind getCardKind();

    public int getPrintedPoints() {
        return printedPoints;
    }

}
