package it.polimi.ingsw.am23.model.cards.turnorder;

import it.polimi.ingsw.am23.exceptions.NoFreeSlotsException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TurnOrderTileTest {

    @Test
    void firstFreeAndOccupiedSlotLookupBehaveAsExpected() {
        // Input  : tile with slots [s1=occupied(p1), s2=free]; then placeTotem on s2.
        // Output : initially getFirstFreeSlot()==s2 and getFirstOccupiedSlot()==s1; tile not empty;
        //          after placing p2 in s2, getFirstFreeSlot() throws NoFreeSlotsException.
        TurnOrderSlot s1 = new TurnOrderSlot(0, 0, "p1");
        TurnOrderSlot s2 = new TurnOrderSlot(0, 0, null);
        TurnOrderTile tile = new TurnOrderTile(List.of(s1, s2));

        assertEquals(s2, tile.getFirstFreeSlot());
        assertEquals(s1, tile.getFirstOccupiedSlot());
        assertFalse(tile.isEmpty());

        s2.placeTotem("p2");
        assertThrows(NoFreeSlotsException.class, tile::getFirstFreeSlot);
    }

    @Test
    void allFreeSlotsReturnsNullForOccupiedAndTrueForEmpty() {
        // Input  : tile with two free slots [s1, s2].
        // Output : getFirstFreeSlot()==s1 (first one), getFirstOccupiedSlot()==null, isEmpty()==true.
        TurnOrderSlot s1 = new TurnOrderSlot(0, 0, null);
        TurnOrderSlot s2 = new TurnOrderSlot(1, 0, null);
        TurnOrderTile tile = new TurnOrderTile(List.of(s1, s2));

        assertEquals(s1, tile.getFirstFreeSlot());
        assertNull(tile.getFirstOccupiedSlot());
        assertTrue(tile.isEmpty());
    }
}
