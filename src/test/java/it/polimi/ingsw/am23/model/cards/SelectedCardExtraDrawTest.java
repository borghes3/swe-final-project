package it.polimi.ingsw.am23.model.cards;

import it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SelectedCardExtraDrawTest {

    @Test
    void isTribeCardDependsOnCardIndexPresence() {
        assertTrue(new SelectedCardExtraDraw(1, null).isTribeCard());
        assertFalse(new SelectedCardExtraDraw(null, 1).isTribeCard());
    }
}
