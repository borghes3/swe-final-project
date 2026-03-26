package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.InventionIcon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventorCardTest {

    @Test
    void constructorRejectsNullIcon() {
        assertThrows(NullPointerException.class, () -> new InventorCard("i1", Era.ERA_1, 0, null, 3));
    }

    @Test
    void getIconReturnsValue() {
        InventorCard card = new InventorCard("i1", Era.ERA_1, 0, InventionIcon.BOAT, 3);
        assertEquals(InventionIcon.BOAT, card.getIcon());
    }
}
