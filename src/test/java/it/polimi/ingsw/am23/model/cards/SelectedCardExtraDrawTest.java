package it.polimi.ingsw.am23.model.cards;

import it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectedCardExtraDrawTest {

    @Test
    void isTribeCardDependsOnCardIndexPresence() {
        // Input  : SelectedCardExtraDraw(cardIndex=1, buildingIndex=null) and (cardIndex=null, buildingIndex=1).
        // Output : first.isTribeCard()==true (cardIndex present), second.isTribeCard()==false (it's a building).
        assertTrue(new SelectedCardExtraDraw(1, null).isTribeCard());
        assertFalse(new SelectedCardExtraDraw(null, 1).isTribeCard());
    }
}
