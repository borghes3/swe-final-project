package it.polimi.ingsw.am23.model.deck;

import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.enums.Era;

import java.util.*;

public class BuildingDeck {

    private Map<Era, Deque<BuildingCard>> cardsByEra;

    public BuildingDeck(Map<Era, List<BuildingCard>> cardsByEra) {
        this.cardsByEra = new EnumMap<>(Era.class);// Map che gestisce enum come key

        for (Era era : Era.values()) {
            List<BuildingCard> cards = cardsByEra.getOrDefault(era, List.of());
            this.cardsByEra.put(era, new ArrayDeque<>(cards));
        }
    }

    public BuildingCard draw(Era era) {
        Deque<BuildingCard> deck = getDeckForEra(era);
        if (deck.isEmpty()) {
            throw new IllegalArgumentException("the deck is empty");
        }
        return deck.removeFirst();
    }

    public BuildingCard peekTop(Era era) {
        Deque<BuildingCard> deck = getDeckForEra(era);
        if (deck.isEmpty()) {
            throw new IllegalArgumentException("the deck is empty");
        }
        return deck.peekFirst();
    }

    private Deque<BuildingCard> getDeckForEra(Era era) {
        return cardsByEra.get(era);
    }

    public boolean isEmpty(Era era) {
        return cardsByEra.isEmpty();
    }

    public int size(Era era) {
        return cardsByEra.get(era).size();
    }

    public Deque<BuildingCard> getCardsForEra(Era era) {
        return cardsByEra.get(era);
    }

}
