package it.polimi.ingsw.am23.model.cards.turnorder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TurnOrderSlotTest {

    @Test
    void placementAndPaymentSemanticsAreConsistent() {
        // Input  : new TurnOrderSlot(position=-2, foodDelta=-2, playerId=null);
        //          then placeTotem("p1") and clear().
        // Output : initial isFree()==true, requiresPayment()==true, givesFood()==false;
        //          after placeTotem isFree()==false; after clear() isFree()==true again.
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
        // Input  : three slots with foodDelta = -3, +2, 0.
        // Output : negative slot requiresPayment()==true & givesFood()==false;
        //          positive slot requiresPayment()==false & givesFood()==true;
        //          zero slot both false.
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
