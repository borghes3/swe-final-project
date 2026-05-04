package it.polimi.ingsw.am23.model.cards.turnorder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TurnOrderSlotTest {

    @Test
    void placementAndPaymentSemanticsAreConsistent() {
        TurnOrderSlot slot = new TurnOrderSlot(-2, -2, null);

        assertTrue(slot.isFree());
        assertTrue(slot.requiresPayment());
        assertFalse(slot.givesFood());

        slot.placeTotem("p1");
        assertFalse(slot.isFree());

        slot.clear();
        assertTrue(slot.isFree());
    }

        @Test
        void foodDeltaConditionsAreCorrect() {
            TurnOrderSlot negativeSlot = new TurnOrderSlot(0, -3, "p1");
            TurnOrderSlot positiveSlot = new TurnOrderSlot(1, 2, "p2");
            TurnOrderSlot zeroSlot = new TurnOrderSlot(2, 0, null);

            assertTrue(negativeSlot.requiresPayment());
            assertFalse(negativeSlot.givesFood());

            assertFalse(positiveSlot.requiresPayment());
            assertTrue(positiveSlot.givesFood());

            assertFalse(zeroSlot.requiresPayment());
            assertFalse(zeroSlot.givesFood());
        }
}
