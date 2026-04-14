package it.polimi.ingsw.am23.model.cards.turnorder;

import it.polimi.ingsw.am23.exceptions.NoFreeSlotsException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TurnOrderTileTest {

    @Test
    void firstFreeAndOccupiedSlotLookupBehaveAsExpected() {
        TurnOrderSlot s1 = new TurnOrderSlot(0, "p1");
        TurnOrderSlot s2 = new TurnOrderSlot(0, null);
        TurnOrderTile tile = new TurnOrderTile(List.of(s1, s2));

        assertEquals(s2, tile.getFirstFreeSlot());
        assertEquals(s1, tile.getFirstOccupiedSlot());
        assertFalse(tile.isEmpty());

        s2.placeTotem("p2");
        assertThrows(NoFreeSlotsException.class, tile::getFirstFreeSlot);
    }
}
