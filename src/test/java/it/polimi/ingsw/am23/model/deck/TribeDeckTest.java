package it.polimi.ingsw.am23.model.deck;

import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.cards.characters.ArtistCard;
import it.polimi.ingsw.am23.model.enums.Era;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TribeDeckTest {

    @Test
    void drawAndPeekRespectOrder() {
        List<Card> cards = List.of(
                new ArtistCard("a1", Era.ERA_1, 0, 2),
                new ArtistCard("a2", Era.ERA_1, 0, 2)
        );
        TribeDeck deck = new TribeDeck(cards);

        assertEquals(2, deck.size());
        assertEquals("a1", deck.peekTop().getId());
        assertEquals(2, deck.size());

        assertEquals("a1", deck.draw().getId());
        assertEquals(1, deck.size());
        assertEquals("a2", deck.peekTop().getId());
    }

    @Test
    void drawAndPeekThrowOnEmptyDeck() {
        TribeDeck deck = new TribeDeck(List.of());
        assertThrows(IllegalStateException.class, deck::draw);
        assertThrows(IllegalStateException.class, deck::peekTop);
    }

    @Test
    void getCardsReturnsUnmodifiableCopy() {
        List<Card> cards = List.of(new ArtistCard("a1", Era.ERA_1, 0, 4));
        TribeDeck deck = new TribeDeck(cards);

        List<Card> snapshot = deck.getCards();
        assertEquals(1, snapshot.size());
        assertEquals("a1", snapshot.getFirst().getId());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(new ArtistCard("a2", Era.ERA_1, 0, 3)));
    }
}
