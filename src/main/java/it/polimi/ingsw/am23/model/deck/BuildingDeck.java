package it.polimi.ingsw.am23.model.deck;

import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.enums.Era;

import java.util.*;

/**
 * Per-era deck of {@link BuildingCard}. Each era owns its own queue and is
 * drawn independently. Used by the card market to refill the top building
 * row when the era advances.
 */
public class BuildingDeck {

    private final Map<Era, Deque<BuildingCard>> cardsByEra;

    /**
     * Builds a new building deck partitioned by era.
     *
     * @param cardsByEra cards per era; missing eras default to an empty deck
     */
    public BuildingDeck(Map<Era, List<BuildingCard>> cardsByEra) {
        this.cardsByEra = new EnumMap<>(Era.class);

        for (Era era : Era.values()) {
            List<BuildingCard> cards = cardsByEra.getOrDefault(era, List.of());
            this.cardsByEra.put(era, new ArrayDeque<>(cards));
        }
    }

    /**
     * Draws the next card from the supplied era's deck.
     *
     * @param era era to draw from
     * @return the drawn card
     * @throws IllegalArgumentException if the era's deck is empty
     */
    public BuildingCard draw(Era era) {
        Deque<BuildingCard> deck = getDeckForEra(era);
        if (deck.isEmpty()) {
            throw new IllegalArgumentException("the deck is empty");
        }
        return deck.removeFirst();
    }

    /**
     * Peeks the next card from the supplied era's deck without drawing it.
     *
     * @param era era to peek
     * @return the next card
     * @throws IllegalArgumentException if the era's deck is empty
     */
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

    /**
     * @param era era to inspect
     * @return {@code true} if the era's deck is empty
     */
    public boolean isEmpty(Era era) {
        return getDeckForEra(era).isEmpty();
    }

    /**
     * @param era era to inspect
     * @return the remaining number of cards in the era's deck
     */
    public int size(Era era) {
        return cardsByEra.get(era).size();
    }

    /**
     * @param era era to inspect
     * @return the live deque backing the era's deck
     */
    public Deque<BuildingCard> getCardsForEra(Era era) {
        return cardsByEra.get(era);
    }

}
