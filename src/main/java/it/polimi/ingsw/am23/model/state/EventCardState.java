package it.polimi.ingsw.am23.model.state;

import it.polimi.ingsw.am23.model.enums.CardKind;
import it.polimi.ingsw.am23.model.enums.Era;

/**
 * Immutable snapshot of an event card.
 */
public final class EventCardState extends CardState {

    /**
     * Builds a new event card snapshot.
     *
     * @param cardId        unique identifier of the card
     * @param era           era the card belongs to
     * @param printedPoints victory points printed on the card
     */
    public EventCardState(String cardId, Era era, int printedPoints) {
        super(cardId, era, printedPoints);
    }

    /** {@inheritDoc} */
    @Override
    public CardKind getCardKind() {
        return CardKind.EVENT;
    }
}
