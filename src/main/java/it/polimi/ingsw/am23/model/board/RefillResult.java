package it.polimi.ingsw.am23.model.board;

import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.enums.Era;

import java.util.ArrayList;
import java.util.List;

/**
 * Outcome of a top row refill performed by {@link
 * CardMarket#refillTopRow}. Reports the freshly added cards and whether the
 * draw triggered an era advance (i.e. a card from a later era surfaced).
 */
public class RefillResult {

    private boolean eraAdvanced;
    private Era newEra;

    private final List<Card> addedCards = new ArrayList<>();

    /** @return {@code true} if the refill triggered an era advance */
    public boolean isEraAdvanced() { return eraAdvanced; }

    /** @return the era to transition into, or {@code null} when no advance was triggered */
    public Era getNewEra() { return newEra; }

    /**
     * Registers that a card belonging to a later era surfaced during the
     * refill, requesting an era advance toward {@code era}. The highest
     * encountered era wins.
     *
     * @param era era discovered on a freshly drawn card
     */
    public void registerEraAdvance(Era era) {
        if (!eraAdvanced || era.ordinal() > newEra.ordinal()) {
            eraAdvanced = true;
            newEra = era;
        }
    }

    /**
     * Registers a card freshly added to the top row.
     *
     * @param card the card added
     */
    public void registerAddedCard(Card card) {
        addedCards.add(card);
    }

    /** @return the cards added during the refill, in draw order */
    public List<Card> getAddedCards() {
        return List.copyOf(addedCards);
    }

}
