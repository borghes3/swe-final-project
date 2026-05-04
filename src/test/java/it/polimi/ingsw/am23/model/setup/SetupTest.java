package it.polimi.ingsw.am23.model.setup;

import it.polimi.ingsw.am23.exceptions.UnmatchedGameCriteriaException;
import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.board.OfferAction;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.cards.events.SustenanceEventCard;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderTile;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SetupTest {

    @Test
    void makeBuildsGameWithMatchingCriteria() {
        Setup setup = new Setup(
                List.of(new PlayerConnectionInfo("p1", "n1"), new PlayerConnectionInfo("p2", "n2")),
                List.of(
                        TestUtils.building("e1-b", Era.ERA_1, 0, 0, new it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect(0)),
                        TestUtils.building("e2-b1", Era.ERA_2, 0, 0, new it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect(0)),
                        TestUtils.building("e2-b2", Era.ERA_2, 0, 0, new it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect(0)),
                        TestUtils.building("e3-b1", Era.ERA_3, 0, 0, new it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect(0)),
                        TestUtils.building("e3-b2", Era.ERA_3, 0, 0, new it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect(0)),
                        TestUtils.building("e3-b3", Era.ERA_3, 0, 0, new it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect(0))
                ),
                List.of(),
                List.of(
                        TestUtils.artist("c1", Era.ERA_1),
                        TestUtils.artist("c2", Era.ERA_1),
                        TestUtils.artist("c3", Era.ERA_1),
                        TestUtils.artist("c4", Era.ERA_1),
                        TestUtils.artist("c5", Era.ERA_1),
                        TestUtils.artist("c6", Era.ERA_1),
                        TestUtils.artist("c7", Era.ERA_1),
                        TestUtils.artist("c8", Era.ERA_1),
                        TestUtils.artist("c9", Era.ERA_1)
                ),
                List.of(
                        new OfferTile('C', null, 3, new OfferAction(1, 0, 0)),
                        new OfferTile('B', null, 2, new OfferAction(1, 0, 0)),
                        new OfferTile('A', null, 1, new OfferAction(1, 0, 0))
                ),
                List.of(new TurnOrderTile(List.of(
                        new TurnOrderSlot(0, 0, null),
                        new TurnOrderSlot(1, 0, null)
                )))
        );

        Game game = setup.make();

        assertEquals(GamePhase.SETUP, game.getGamePhase());
        assertEquals(List.of('A', 'B'), game.getBoard().getFreeOfferTiles().stream().map(OfferTile::getId).toList());
        assertEquals(List.of(2, 3), game.getPlayers().stream().map(Player::getFood).sorted().toList());
        assertEquals(2, game.getBoard().getTurnOrderTile().getSlotsCount());
    }

    @Test
    void makeRejectsNoMatchingOfferTiles() {
        Setup setup = new Setup(
                List.of(new PlayerConnectionInfo("p1", "n1"), new PlayerConnectionInfo("p2", "n2")),
                List.of(),
                List.of(),
                List.of(TestUtils.artist("c1", Era.ERA_1), TestUtils.artist("c2", Era.ERA_1), TestUtils.artist("c3", Era.ERA_1), TestUtils.artist("c4", Era.ERA_1), TestUtils.artist("c5", Era.ERA_1), TestUtils.artist("c6", Era.ERA_1), TestUtils.artist("c7", Era.ERA_1), TestUtils.artist("c8", Era.ERA_1), TestUtils.artist("c9", Era.ERA_1)),
                List.of(new OfferTile('A', null, 3, new OfferAction(1, 0, 0))),
                List.of(new TurnOrderTile(List.of(new TurnOrderSlot(0, 0, null), new TurnOrderSlot(1, 0, null))))
        );

        assertThrows(UnmatchedGameCriteriaException.class, setup::make);
    }

    @Test
    void makeRejectsNoMatchingTurnOrderTile() {
        Setup setup = new Setup(
                List.of(new PlayerConnectionInfo("p1", "n1"), new PlayerConnectionInfo("p2", "n2")),
                List.of(),
                List.of(),
                List.of(TestUtils.artist("c1", Era.ERA_1), TestUtils.artist("c2", Era.ERA_1), TestUtils.artist("c3", Era.ERA_1), TestUtils.artist("c4", Era.ERA_1), TestUtils.artist("c5", Era.ERA_1), TestUtils.artist("c6", Era.ERA_1), TestUtils.artist("c7", Era.ERA_1), TestUtils.artist("c8", Era.ERA_1), TestUtils.artist("c9", Era.ERA_1)),
                List.of(new OfferTile('A', null, 1, new OfferAction(1, 0, 0))),
                List.of(new TurnOrderTile(List.of(new TurnOrderSlot(0, 0, null), new TurnOrderSlot(1, 0, null), new TurnOrderSlot(2, 0, null))))
        );

        assertThrows(UnmatchedGameCriteriaException.class, setup::make);
    }

    @Test
    void drawCardsSeparatesEventsAndCharactersByRow() throws Exception {
        Setup setup = new Setup(
                List.of(new PlayerConnectionInfo("p1", "n1"), new PlayerConnectionInfo("p2", "n2")),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        Method drawCards = Setup.class.getDeclaredMethod("drawCards", it.polimi.ingsw.am23.model.deck.TribeDeck.class);
        drawCards.setAccessible(true);

        Object drawResult = drawCards.invoke(setup, new it.polimi.ingsw.am23.model.deck.TribeDeck(List.of(
                new SustenanceEventCard("e1", Era.ERA_1, 0, false),
                TestUtils.artist("c1", Era.ERA_1),
                TestUtils.artist("c2", Era.ERA_1),
                TestUtils.artist("c3", Era.ERA_1),
                TestUtils.artist("c4", Era.ERA_1),
                TestUtils.artist("c5", Era.ERA_1),
                TestUtils.artist("c6", Era.ERA_1),
                TestUtils.artist("c7", Era.ERA_1),
                TestUtils.artist("c8", Era.ERA_1)
        )));

        Method upperRow = drawResult.getClass().getDeclaredMethod("upperRow");
        Method lowerRow = drawResult.getClass().getDeclaredMethod("lowerRow");
        upperRow.setAccessible(true);
        lowerRow.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Card> upper = (List<Card>) upperRow.invoke(drawResult);
        @SuppressWarnings("unchecked")
        List<Card> lower = (List<Card>) lowerRow.invoke(drawResult);

        assertEquals(6, upper.size());
        assertEquals(3, lower.size());
        assertTrue(upper.stream().anyMatch(card -> card.getId().equals("e1")));
        assertTrue(lower.stream().allMatch(card -> card.getId().startsWith("c")));
    }
}