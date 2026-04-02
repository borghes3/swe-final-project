package it.polimi.ingsw.am23.model.state;

import it.polimi.ingsw.am23.model.enums.CardKind;
import it.polimi.ingsw.am23.model.enums.Era;

public final class EventCardState extends CardState {

    public EventCardState(String cardId, Era era, int printedPoints) {
        super(cardId, era, printedPoints);
    }

    @Override
    public CardKind getCardKind() {
        return CardKind.EVENT;
    }
}
