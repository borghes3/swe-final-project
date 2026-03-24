package it.polimi.ingsw.am23.model.board;

import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.cards.EventCard;
import it.polimi.ingsw.am23.model.deck.TribeDeck;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.RowType;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.Game;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CardMarketTest {

    @Test
    void getAndRemoveCardByRow() {
        List<Card> topRow = new ArrayList<>(List.of(new TestCard("c1", Era.ERA_1), new TestCard("c2", Era.ERA_1)));
        List<Card> bottomRow = new ArrayList<>(List.of(new TestCard("c3", Era.ERA_1)));
        CardMarket market = new CardMarket(topRow, bottomRow, List.of(), List.of());

        assertEquals("c1", market.getCard(RowType.TOP, 0).getId());
        assertEquals("c1", market.removeCard(RowType.TOP, 0).getId());
        assertEquals(1, market.getRowSize(RowType.TOP));
    }

    @Test
    void getCardRejectsInvalidIndex() {
        CardMarket market = new CardMarket(List.of(), List.of(), List.of(), List.of());
        assertThrows(IndexOutOfBoundsException.class, () -> market.getCard(RowType.TOP, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> market.getCard(RowType.BOTTOM, -1));
    }

    @Test
    void addCardUpdateSizes() {
        CardMarket market = new CardMarket(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        market.addCardToRow(RowType.TOP, new TestCard("c1", Era.ERA_1));
        market.addBuilding(RowType.BOTTOM, building("b1"));

        assertEquals(1, market.getRowSize(RowType.TOP));
        assertEquals(1, market.getBuildingCount(RowType.BOTTOM));
    }

    @Test
    void refillDetectsEraAdvance() {
        CardMarket market = new CardMarket(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        TribeDeck deck = new TribeDeck(List.of(new TestCard("c1", Era.ERA_1), new TestCard("c2", Era.ERA_2)));

        RefillResult result = market.refillTopRow(deck, 2, Era.ERA_1);

        assertEquals(2, market.getRowSize(RowType.TOP));
        assertTrue(result.isEraAdvanced());
        assertEquals(Era.ERA_2, result.getNewEra());
    }

    @Test
    void refillTopRowRejectsInvalidInput() {
        CardMarket market = new CardMarket(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        TribeDeck deck = new TribeDeck(List.of());

        assertThrows(NullPointerException.class, () -> market.refillTopRow(null, 1, Era.ERA_1));
        assertThrows(NullPointerException.class, () -> market.refillTopRow(deck, 1, null));
        assertThrows(IllegalArgumentException.class, () -> market.refillTopRow(deck, -1, Era.ERA_1));
    }

    @Test
    void bottomRowEventsFiltersEventCards() {
        List<Card> bottomRow = new ArrayList<>(List.of(new TestEventCard("e1", Era.ERA_1), new TestCard("c1", Era.ERA_1)));
        CardMarket market = new CardMarket(List.of(), bottomRow, List.of(), List.of());

        List<EventCard> events = market.getBottomRowEvents();
        assertEquals(1, events.size());
        assertEquals(TestEventCard.class, events.getFirst().getClass());
    }

    @Test
    void moveTopRowToBottomClearsTop() {
        List<Card> topRow = new ArrayList<>(List.of(new TestCard("c1", Era.ERA_1)));
        CardMarket market = new CardMarket(topRow, new ArrayList<>(), List.of(), List.of());

        market.moveTopRowToBottom();

        assertEquals(0, market.getRowSize(RowType.TOP));
        assertEquals(1, market.getRowSize(RowType.BOTTOM));
        assertEquals("c1", market.getCard(RowType.BOTTOM, 0).getId());
    }

    @Test
    void clearBottomRowRemovesAllCards() {
        List<Card> bottomRow = new ArrayList<>(List.of(new TestCard("c1", Era.ERA_1)));
        CardMarket market = new CardMarket(new ArrayList<>(), bottomRow, List.of(), List.of());

        market.clearBottomRow();

        assertEquals(0, market.getRowSize(RowType.BOTTOM));
    }

    private static BuildingCard building(String id) {
        return new BuildingCard(id, Era.ERA_1, 0, 0, new BuildingEffect() {
        });
    }

    private static class TestCard extends Card {
        TestCard(String id, Era era) {
            super(id, era, 0);
        }

        @Override
        public boolean canBeTaken() {
            return true;
        }

        @Override
        public void onTaken(Game game, Player player) {
        }
    }

    private static class TestEventCard extends EventCard {
        TestEventCard(String id, Era era) {
            super(id, era, 0);
        }

        @Override
        public void resolve(Game game) {
        }
    }
}
