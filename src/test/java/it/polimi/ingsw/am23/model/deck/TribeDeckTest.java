package it.polimi.ingsw.am23.model.deck;

import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.enums.Era;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TribeDeckTest {

    @Test
    void drawPeekAndEmptyChecksAreCoherent() {
        TribeDeck deck = new TribeDeck(List.of(
                TestUtils.artist("a1", Era.ERA_1),
                TestUtils.artist("a2", Era.ERA_1)
        ));

        assertEquals("a1", deck.peekTop().getId());
        assertEquals("a1", deck.draw().getId());
        assertEquals(1, deck.size());
        assertFalse(deck.isEmpty());
        assertEquals(1, deck.getCards().size());

        deck.draw();
        assertTrue(deck.isEmpty());
        assertThrows(IllegalStateException.class, deck::draw);
        assertThrows(IllegalStateException.class, deck::peekTop);
    }
}
