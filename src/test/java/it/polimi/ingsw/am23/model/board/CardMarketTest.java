package it.polimi.ingsw.am23.model.board;

import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.deck.BuildingDeck;
import it.polimi.ingsw.am23.model.deck.TribeDeck;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.RowType;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CardMarketTest {

    @Test
    void cardAndBuildingOperationsUseCorrectRows() {
        Card top = TestUtils.artist("a1", Era.ERA_1);
        Card bottom = TestUtils.artist("a2", Era.ERA_1);
        CardMarket market = new CardMarket(List.of(top), List.of(bottom), List.of(TestUtils.building("b1", Era.ERA_1, 0, 0, new it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect(0))));

        assertEquals(top, market.getCard(RowType.TOP, 0));
        assertEquals(bottom, market.getCard(RowType.BOTTOM, 0));
        assertEquals(1, market.getRowSize(RowType.TOP));

        market.removeCard(RowType.BOTTOM, 0);
        assertEquals(0, market.getRowSize(RowType.BOTTOM));

        market.addCardToRow(RowType.BOTTOM, TestUtils.artist("a3", Era.ERA_1));
        market.addBuilding(RowType.BOTTOM, TestUtils.building("b2", Era.ERA_1, 0, 0, new it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect(0)));
        market.removeBuilding(RowType.TOP, 0);

        assertEquals(2, market.getDrawableCount(RowType.BOTTOM));
    }

    @Test
    void refillTopRowRejectsInvalidNumberOfPlayers() {
        CardMarket market = new CardMarket(List.of(), List.of(), List.of());
        TribeDeck deck = new TribeDeck(List.of(TestUtils.artist("a1", Era.ERA_1)));

        assertThrows(IllegalArgumentException.class, () -> market.refillTopRow(deck, 1, Era.ERA_1));
    }

    @Test
    void refillTopRowStopsAtTargetSizeAndTracksHighestAdvancedEra() {
        CardMarket market = new CardMarket(List.of(), List.of(), List.of());
        TribeDeck deck = new TribeDeck(List.of(
                TestUtils.artist("a1", Era.ERA_1),
                TestUtils.artist("a2", Era.ERA_2),
                TestUtils.artist("a3", Era.ERA_2),
                TestUtils.artist("a4", Era.ERA_3),
                TestUtils.artist("a5", Era.ERA_1),
                TestUtils.artist("a6", Era.ERA_2),
                TestUtils.artist("a7", Era.ERA_1)
        ));

        RefillResult result = market.refillTopRow(deck, 2, Era.ERA_1);

        assertEquals(6, market.getTopRow().size());
        assertEquals(1, deck.size());
        assertTrue(result.isEraAdvanced());
        assertEquals(Era.ERA_3, result.getNewEra());
    }

    @Test
    void resolvingAndEraProgressionHelpersWork() {
        CardMarket market = new CardMarket(
                List.of(
                        new it.polimi.ingsw.am23.model.cards.events.SustenanceEventCard("s-top", Era.ERA_1, 0, false),
                        TestUtils.artist("a-top", Era.ERA_1)
                ),
                List.of(new it.polimi.ingsw.am23.model.cards.events.SustenanceEventCard("s-bottom", Era.ERA_1, 0, false)),
                List.of(TestUtils.building("top-1", Era.ERA_1, 0, 0, new it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect(0)))
        );

        assertEquals(1, market.getBottomRowEvents().size());
        assertEquals(1, market.getTopRowEvents().size());

        market.clearBottomRow();
        assertTrue(market.getBottomRow().isEmpty());

        market.moveTopRowToBottom();
        assertEquals(2, market.getBottomRow().size());

        Map<Era, List<it.polimi.ingsw.am23.model.cards.BuildingCard>> byEra = new EnumMap<>(Era.class);
        byEra.put(Era.ERA_2, List.of(TestUtils.building("e2", Era.ERA_2, 0, 0, new it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect(0))));
        BuildingDeck deck = new BuildingDeck(byEra) {
            @Override
            public boolean isEmpty(Era era) {
                return size(era) == 0;
            }
        };

        market.handleEraProgression(deck, Era.ERA_2);

        assertEquals(1, market.getBottomBuildings().size());
        assertEquals("e2", market.getTopBuildings().get(0).getId());
    }
}
