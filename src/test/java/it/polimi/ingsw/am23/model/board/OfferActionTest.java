package it.polimi.ingsw.am23.model.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OfferActionTest {

    @Test
    void gettersReturnConfiguredValues() {
        OfferAction action = new OfferAction(1, 2, 3);
        assertEquals(1, action.getUpperDrawRowCount());
        assertEquals(2, action.getBottomDrawCount());
        assertEquals(3, action.getFoodReward());
    }
}
