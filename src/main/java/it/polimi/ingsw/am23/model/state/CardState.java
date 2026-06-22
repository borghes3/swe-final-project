package it.polimi.ingsw.am23.model.state;

import it.polimi.ingsw.am23.model.enums.CardKind;
import it.polimi.ingsw.am23.model.enums.Era;

import java.io.Serializable;
import java.util.Objects;

/**
 * Common base class for the serializable, immutable snapshots of any card
 * exposed by the model to its clients.
 */
public abstract class CardState implements Serializable {

    private final String cardId;
    private final Era era;
    private final int printedPoints;

    /**
     * Initializes the shared card metadata.
     *
     * @param cardId        unique identifier of the card
     * @param era           era the card belongs to
     * @param printedPoints victory points printed on the card
     */
    protected CardState(String cardId,
                        Era era,
                        int printedPoints) {
        this.cardId = Objects.requireNonNull(cardId, "cardId cannot be null");
        this.era = Objects.requireNonNull(era, "era cannot be null");
        this.printedPoints = printedPoints;
    }

    /**
     * @return the unique identifier of this card
     */
    public String getCardId() {
        return cardId;
    }

    /**
     * @return the era the card belongs to
     */
    public Era getEra() {
        return era;
    }

    /**
     * @return the high level kind of this card (character, event or building)
     */
    public abstract CardKind getCardKind();

    /**
     * @return the victory points printed on the card
     */
    public int getPrintedPoints() {
        return printedPoints;
    }

}
