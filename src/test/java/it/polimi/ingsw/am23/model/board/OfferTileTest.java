package it.polimi.ingsw.am23.model.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OfferTileTest {

    @Test
    void isFreeReflectsOccupiedState() {
        OfferAction action = new OfferAction(0, 0, 0);
        OfferTile tile = new OfferTile('A', null, 2, action);

        assertTrue(tile.isFree());
        tile.placeTotem("player-1");
        assertFalse(tile.isFree());
        assertEquals("player-1", tile.getOccupiedByPlayerId());

        tile.clear();
        assertTrue(tile.isFree());
    }

    @Test
    void gettersReturnValues() {
        OfferAction action = new OfferAction(1, 0, 2);
        OfferTile tile = new OfferTile('B', "p1", 3, action);

        assertEquals('B', tile.getId());
        assertEquals("p1", tile.getOccupiedByPlayerId());
        assertEquals(3, tile.getMinPlayers());
        assertSame(action, tile.getAction());
    }
}
