package it.polimi.ingsw.am23.model.board;

import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.enums.Era;

import java.util.ArrayList;
import java.util.List;

public class RefillResult {

    private boolean eraAdvanced;
    private Era newEra;

    // Carte aggiunte alla fila superiore durante il refill
    private final List<Card> addedCards = new ArrayList<>();

    public boolean isEraAdvanced() { return eraAdvanced; }
    public Era getNewEra() { return newEra; }

    public void registerEraAdvance(Era era) {
        if (!eraAdvanced || era.ordinal() > newEra.ordinal()) {
            eraAdvanced = true;
            newEra = era;
        }
    }

    public void registerAddedCard(Card card) {
        addedCards.add(card);
    }

    // Carte aggiunte alla topRow durante il refill (escluse quelle già presenti)
    public List<Card> getAddedCards() {
        return List.copyOf(addedCards);
    }

}
