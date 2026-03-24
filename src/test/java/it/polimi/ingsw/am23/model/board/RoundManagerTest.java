package it.polimi.ingsw.am23.model.board;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoundManagerTest {

    @Test
    void constructorRejectsEmptyPlacingOrder() {
        assertThrows(IllegalArgumentException.class, () -> new RoundManager(List.of()));
    }

    @Test
    void placingPhaseAdvancesAndCompletes() {
        RoundManager manager = new RoundManager(List.of("p1", "p2"));

        assertEquals("p1", manager.getCurrentPlacingPlayerId());
        manager.advancePlacing();
        assertEquals("p2", manager.getCurrentPlacingPlayerId());
        manager.advancePlacing();
        assertTrue(manager.isPlacingPhaseComplete());
        assertNull(manager.getCurrentPlacingPlayerId());
    }

    @Test
    void resolvingPhaseAdvancesAndCompletes() {
        RoundManager manager = new RoundManager(List.of("p1"));
        manager.setResolvingOrder(List.of("p2", "p3"));

        assertEquals("p2", manager.getCurrentResolvingPlayerId());
        manager.advanceResolving();
        assertEquals("p3", manager.getCurrentResolvingPlayerId());
        manager.advanceResolving();
        assertTrue(manager.isResolvingPhaseComplete());
        assertNull(manager.getCurrentResolvingPlayerId());
    }

    @Test
    void setNextRoundOrderResetsState() {
        RoundManager manager = new RoundManager(List.of("p1", "p2"));
        manager.advancePlacing();
        manager.setResolvingOrder(List.of("p2"));

        manager.setNextRoundOrder(List.of("p3", "p4"));

        assertEquals(0, manager.getPlacingIndex());
        assertEquals(0, manager.getResolvingIndex());
        assertEquals("p3", manager.getCurrentPlacingPlayerId());
        assertTrue(manager.getResolvingOrder().isEmpty());
    }

    @Test
    void setNextRoundOrderRejectsEmptyList() {
        RoundManager manager = new RoundManager(List.of("p1"));
        assertThrows(IllegalArgumentException.class, () -> manager.setNextRoundOrder(List.of()));
    }

    @Test
    void resetResolvingPhaseClearsOrder() {
        RoundManager manager = new RoundManager(List.of("p1"));
        manager.setResolvingOrder(List.of("p2"));

        manager.resetResolvingPhase();

        assertTrue(manager.getResolvingOrder().isEmpty());
        assertEquals(0, manager.getResolvingIndex());
    }
}
