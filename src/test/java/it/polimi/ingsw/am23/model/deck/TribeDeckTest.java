package it.polimi.ingsw.am23.model.deck;

import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.enums.Era;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TribeDeckTest {

    @Test
    void drawPeekAndEmptyChecksAreCoherent() {
        // Input  : TribeDeck of [a1, a2]; peekTop, draw twice, then attempt one more draw and peek.
        // Output : peekTop().id=="a1", first draw().id=="a1", size==1, isEmpty==false, getCards().size()==1;
        //          after second draw isEmpty==true; further draw()/peekTop() throw IllegalStateException.
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
