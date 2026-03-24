package it.polimi.ingsw.am23.model.board;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void constructorRejectsNullArguments() {
        CardMarket market = new CardMarket(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        List<OfferTile> tiles = List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0)));
        List<TurnOrderSlot> slots = List.of(new TurnOrderSlot(0, 0, null));

        assertThrows(NullPointerException.class, () -> new Board(null, tiles, slots));
        assertThrows(NullPointerException.class, () -> new Board(market, null, slots));
        assertThrows(NullPointerException.class, () -> new Board(market, tiles, null));
    }

    @Test
    void findOccupiedSlotsAndTiles() {
        OfferTile tile1 = new OfferTile('A', "p1", 2, new OfferAction(0, 0, 0));
        OfferTile tile2 = new OfferTile('B', null, 2, new OfferAction(0, 0, 0));
        TurnOrderSlot slot1 = new TurnOrderSlot(0, 0, "p2");
        TurnOrderSlot slot2 = new TurnOrderSlot(1, 0, null);

        Board board = new Board(new CardMarket(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                List.of(tile1, tile2), List.of(slot1, slot2));

        assertSame(tile1, board.findOfferTileOccupiedBy("p1"));
        assertNull(board.findOfferTileOccupiedBy("missing"));
        assertSame(slot1, board.findTurnOrderSlotOccupiedBy("p2"));
        assertNull(board.findTurnOrderSlotOccupiedBy("missing"));
    }

    @Test
    void freeListsAndClearOperationsWork() {
        OfferTile tile1 = new OfferTile('A', "p1", 2, new OfferAction(0, 0, 0));
        OfferTile tile2 = new OfferTile('B', null, 2, new OfferAction(0, 0, 0));
        TurnOrderSlot slot1 = new TurnOrderSlot(0, 0, "p2");
        TurnOrderSlot slot2 = new TurnOrderSlot(1, 0, null);

        Board board = new Board(new CardMarket(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                List.of(tile1, tile2), List.of(slot1, slot2));

        assertEquals(1, board.getFreeOfferTiles().size());
        assertEquals(1, board.getFreeTurnOrderSlots().size());

        board.clearOfferTiles();
        board.clearTurnOrderSlots();

        assertEquals(2, board.getFreeOfferTiles().size());
        assertEquals(2, board.getFreeTurnOrderSlots().size());
    }

    @Test
    void gettersExposeListsAndMarket() {
        CardMarket market = new CardMarket(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        OfferTile tile = new OfferTile('A', null, 2, new OfferAction(0, 0, 0));
        TurnOrderSlot slot = new TurnOrderSlot(0, 0, null);
        Board board = new Board(market, List.of(tile), List.of(slot));

        assertSame(market, board.getMarket());
        assertEquals(1, board.getOfferTiles().size());
        assertEquals(1, board.getTurnOrderSlots().size());
        assertSame(tile, board.getOfferTile(0));
        assertSame(slot, board.getTurnOrderSlot(0));
    }
}
