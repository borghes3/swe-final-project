package it.polimi.ingsw.am23.model.board;

import it.polimi.ingsw.am23.exceptions.OfferTileNotFoundException;
import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderTile;
import it.polimi.ingsw.am23.model.enums.Era;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void tileAndSlotLookupAndFreeListsWork() {
        OfferTile tileA = new OfferTile('A', "p1", 2, new OfferAction(1, 0, 0));
        OfferTile tileB = new OfferTile('B', null, 2, new OfferAction(0, 1, 0));
        TurnOrderSlot s1 = new TurnOrderSlot(0, 0,null);
        TurnOrderSlot s2 = new TurnOrderSlot(0, 0,"p2");

        Board board = new Board(List.of(tileA, tileB), new TurnOrderTile(List.of(s1, s2)));

        assertEquals(tileA, board.getOfferTile('A'));
        assertThrows(OfferTileNotFoundException.class, () -> board.getOfferTile('X'));
        assertEquals(tileA, board.getOfferTileByPlayerId("p1"));
        assertEquals(tileA, board.getFirstOccupiedOfferTile());
        assertEquals(s2, board.findTurnOrderSlotOccupiedBy("p2"));
        assertEquals(1, board.getFreeOfferTiles().size());
        assertEquals(1, board.getFreeTurnOrderSlots().size());
    }

    @Test
    void stateBuildersProduceCoherentStateShape() {
        OfferTile tileA = new OfferTile('A', "p1", 2, new OfferAction(1, 0, 1));
        TurnOrderSlot slot = new TurnOrderSlot(2,0, "p1");
        Board board = new Board(List.of(tileA), new TurnOrderTile(List.of(slot)));
        CardMarket market = new CardMarket(List.of(TestUtils.artist("a1", Era.ERA_1)), List.of(), List.of());

        assertEquals(1, board.buildOfferTileState().size());
        assertEquals(1, board.buildTurnOrderSlotsState().size());
        assertEquals(1, board.getState(market).getTopRow().size());
        assertThrows(NullPointerException.class, () -> board.findTurnOrderSlotOccupiedBy(null));
    }
}
