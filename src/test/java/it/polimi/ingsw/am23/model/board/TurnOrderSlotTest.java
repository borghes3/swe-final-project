package it.polimi.ingsw.am23.model.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TurnOrderSlotTest {

    @Test
    void isFreeAndPlaceClearWork() {
        TurnOrderSlot slot = new TurnOrderSlot(0, 0, null);
        assertTrue(slot.isFree());

        slot.placeTotem("p1");
        assertFalse(slot.isFree());
        assertEquals("p1", slot.getOccupiedByPlayerId());

        slot.clear();
        assertTrue(slot.isFree());
    }

    @Test
    void paymentFlagsDependOnFoodDelta() {
        TurnOrderSlot paying = new TurnOrderSlot(0, -2, null);
        TurnOrderSlot giving = new TurnOrderSlot(1, 3, null);
        TurnOrderSlot neutral = new TurnOrderSlot(2, 0, null);

        assertTrue(paying.requiresPayment());
        assertFalse(paying.givesFood());

        assertTrue(giving.givesFood());
        assertFalse(giving.requiresPayment());

        assertFalse(neutral.requiresPayment());
        assertFalse(neutral.givesFood());
    }
}
