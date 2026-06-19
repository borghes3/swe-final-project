package it.polimi.ingsw.am23.model.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OfferTileTest {

    @Test
    void placeAndClearTotemChangesOccupancyState() {
        // Input  : free OfferTile('A'); placeTotem("p1"); then clear().
        // Output : initially isFree==true; after placeTotem isFree==false; after clear isFree==true again.
        OfferTile tile = new OfferTile('A', null, 2, new OfferAction(1, 1, 0));

        assertTrue(tile.isFree());
        tile.placeTotem("p1");
        assertFalse(tile.isFree());
        tile.clear();
        assertTrue(tile.isFree());
    }
}
