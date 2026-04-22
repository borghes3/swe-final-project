package it.polimi.ingsw.am23.model.cards.turnorder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TurnOrderSlotTest {

    @Test
    void placementAndPaymentSemanticsAreConsistent() {
        TurnOrderSlot slot = new TurnOrderSlot(-2, 0,null);

        assertTrue(slot.isFree());
        assertTrue(slot.requiresPayment());
        assertFalse(slot.givesFood());

        slot.placeTotem("p1");
        assertFalse(slot.isFree());

        slot.clear();
        assertTrue(slot.isFree());
    }
}
