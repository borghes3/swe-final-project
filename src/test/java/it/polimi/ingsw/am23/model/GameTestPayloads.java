package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.exceptions.IllegalActionException;
import it.polimi.ingsw.am23.exceptions.PlayerNotFoundException;
import it.polimi.ingsw.am23.model.board.OfferAction;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.cards.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.model.cards.SelectedCards;
import it.polimi.ingsw.am23.model.cards.events.SustenanceEventCard;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.resolvers.ScoreResult;
import it.polimi.ingsw.am23.model.state.GameState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
/*
class GameTestPayloads {

    @Test
    void startGameMovesToPlacingTotemsAndNotifiesObservers() {
        Player p1 = TestUtils.player("p1", 3, 0);
        Game game = TestUtils.game(
                List.of(p1),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, "p1")),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );
        RecordingObserverPayloads observer = new RecordingObserverPayloads();
        game.addObserver(observer);

        game.startGame();

        assertEquals(GamePhase.PLACING_TOTEMS, game.getGamePhase());
        assertEquals(1, observer.gameStartedCount);
        assertEquals(1, observer.stateChangedCount);
    }

    @Test
    void findPlayerThrowsForMissingPlayer() {
        Player p1 = TestUtils.player("p1", 3, 0);
        Game game = TestUtils.game(
                List.of(p1),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, null)),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        assertThrows(PlayerNotFoundException.class, () -> game.findPlayer("missing"));
    }

    @Test
    void placeTotemRejectsWrongPlayerInTurnOrder() {
        Player p1 = TestUtils.player("p1", 3, 0);
        Player p2 = TestUtils.player("p2", 3, 0);

        Game game = TestUtils.game(
                List.of(p1, p2),
                List.of(
                        new OfferTile('A', null, 2, new OfferAction(1, 0, 0)),
                        new OfferTile('B', null, 2, new OfferAction(0, 1, 0))
                ),
                List.of(
                        new TurnOrderSlot(0, "p1"),
                        new TurnOrderSlot(0, "p2")
                ),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        ActionResult result = game.placeTotem("p2", 'A');

        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.WRONG_PLAYER, result.getError());
        assertTrue(game.getBoard().getOfferTile('A').isFree());
        assertEquals("p1", game.getBoard().getTurnOrderTile().getSlot(0).getPlayerId());
    }

    @Test
    void placeTotemTransitionsToResolvingOffersWhenLastTotemIsPlaced() {
        Player p1 = TestUtils.player("p1", 3, 0);
        Player p2 = TestUtils.player("p2", 3, 0);

        Game game = TestUtils.game(
                List.of(p1, p2),
                List.of(
                        new OfferTile('A', null, 2, new OfferAction(1, 0, 0)),
                        new OfferTile('B', null, 2, new OfferAction(0, 1, 0))
                ),
                List.of(
                        new TurnOrderSlot(0, "p1"),
                        new TurnOrderSlot(0, "p2")
                ),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        game.startGame();
        assertTrue(game.placeTotem("p1", 'A').isSuccess());
        ActionResult secondPlacement = game.placeTotem("p2", 'B');

        assertTrue(secondPlacement.isSuccess());
        assertEquals(GamePhase.RESOLVING_OFFERS, game.getGamePhase());
        assertTrue(game.getBoard().getTurnOrderTile().isEmpty());
    }

    @Test
    void takeCardsThrowsWhenSelectionDoesNotMatchOfferCriteria() {
        Player p1 = TestUtils.player("p1", 3, 0);
        Player p2 = TestUtils.player("p2", 3, 0);

        Game game = TestUtils.game(
                List.of(p1, p2),
                List.of(new OfferTile('A', "p1", 2, new OfferAction(1, 0, 0))),
                List.of(new TurnOrderSlot(0, null), new TurnOrderSlot(0, "p2")),
                List.of(TestUtils.artist("a1", Era.ERA_1)),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        assertThrows(IllegalActionException.class, () -> game.takeCards("p1", new SelectedCards(List.of(), List.of(), List.of(), List.of())));
    }

    @Test
    void takeCardsRejectsEventCardsAndDoesNotConsumeTurn() {
        Player p1 = TestUtils.player("p1", 3, 0);
        Player p2 = TestUtils.player("p2", 3, 0);

        Card topEvent = new SustenanceEventCard("se1", Era.ERA_1, 0, false);
        Game game = TestUtils.game(
                List.of(p1, p2),
                List.of(new OfferTile('A', "p1", 2, new OfferAction(1, 0, 0))),
                List.of(new TurnOrderSlot(0, null), new TurnOrderSlot(0, "p2")),
            List.of(topEvent, TestUtils.artist("a1", Era.ERA_1)),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        ActionResult result = game.takeCards("p1", new SelectedCards(List.of(0), List.of(), List.of(), List.of()));

        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.CARD_NOT_TAKABLE, result.getError());
        assertNotNull(game.getBoard().getOfferTileByPlayerId("p1"));
    }

    @Test
    void takeCardsSuccessReturnsPlayerToTurnOrderAndGrantsFoodReward() {
        Player p1 = TestUtils.player("p1", 0, 0);
        Player p2 = TestUtils.player("p2", 0, 0);

        Game game = TestUtils.game(
                List.of(p1, p2),
                List.of(new OfferTile('A', "p1", 2, new OfferAction(1, 0, 2))),
                List.of(new TurnOrderSlot(1, null), new TurnOrderSlot(0, "p2")),
                List.of(TestUtils.artist("a1", Era.ERA_1)),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        ActionResult result = game.takeCards("p1", new SelectedCards(List.of(0), List.of(), List.of(), List.of()));

        assertTrue(result.isSuccess());
        assertEquals(3, p1.getFood());
        assertEquals(1, p1.getTribe().getCharacters().size());
        assertEquals("p1", game.getBoard().getTurnOrderTile().getSlot(0).getPlayerId());
    }

    @Test
    void takeExtraCardValidatesPendingPlayerAndCost() {
        Player p1 = TestUtils.player("p1", 1, 0);
        Player p2 = TestUtils.player("p2", 3, 0);

        BuildingCard expensiveBuilding = TestUtils.building("b1", Era.ERA_1, 0, 4, new it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect(0));
        Game game = TestUtils.game(
                List.of(p1, p2),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, null), new TurnOrderSlot(0, null)),
                List.of(TestUtils.artist("a1", Era.ERA_1)),
                List.of(),
                List.of(expensiveBuilding),
                Era.ERA_1,
                1
        );

        assertEquals(ErrorCode.NO_PENDING_EXTRA_DRAW, game.takeExtraCard("p1", new SelectedCardExtraDraw(0, null)).getError());

        game.setPendingExtraDrawPlayerId("p1");
        assertEquals(ErrorCode.INVALID_EXTRA_DRAW, game.takeExtraCard("p2", new SelectedCardExtraDraw(0, null)).getError());
        assertEquals(ErrorCode.NOT_ENOUGH_FOOD, game.takeExtraCard("p1", new SelectedCardExtraDraw(null, 0)).getError());
    }

    @Test
    void applyFoodCostWithPointsFallbackUsesFoodThenPrestige() {
        Player p1 = TestUtils.player("p1", 5, 10);
        Player p2 = TestUtils.player("p2", 0, 10);

        Game game = TestUtils.game(
                List.of(p1, p2),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, null), new TurnOrderSlot(0, null)),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_2,
                1
        );

        game.applyFoodCostWithPointsFallback(p1, 3);
        game.applyFoodCostWithPointsFallback(p2, 3);

        assertEquals(2, p1.getFood());
        assertEquals(7, p2.getPrestigePoints());
    }

    @Test
    void resolveEventsAndCalculateScoresProduceObserverNotifications() {
        Player p1 = TestUtils.player("p1", 3, 0);
        Player p2 = TestUtils.player("p2", 3, 0);
        Game game = TestUtils.game(
                List.of(p1, p2),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, null), new TurnOrderSlot(0, null)),
                List.of(),
                List.of(new SustenanceEventCard("s", Era.ERA_1, 0, false)),
                List.of(),
                Era.ERA_1,
                1
        );

        RecordingObserverPayloads observer = new RecordingObserverPayloads();
        game.addObserver(observer);

        ActionResult eventsResult = game.resolveEvents();
        ActionResult scoresResult = game.calculateScores();

        assertTrue(eventsResult.isSuccess());
        assertTrue(scoresResult.isSuccess());
        assertEquals(1, observer.endResolvingCount);
        assertEquals(1, observer.scoresCount);

        game.removeObserver(observer);
        game.calculateScores();
        assertEquals(1, observer.scoresCount);
    }

    @Test
    void resolveEventsAtRoundTenEndsGameAndNotifiesGameOver() {
        Player p1 = TestUtils.player("p1", 3, 0);
        Player p2 = TestUtils.player("p2", 3, 0);
        Game game = TestUtils.game(
                List.of(p1, p2),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, null), new TurnOrderSlot(0, null)),
                List.of(new SustenanceEventCard("s-top", Era.ERA_1, 0, true)),
                List.of(new SustenanceEventCard("s-bottom", Era.ERA_1, 0, false)),
                List.of(),
                Era.ERA_1,
                10
        );
        RecordingObserverPayloads observer = new RecordingObserverPayloads();
        game.addObserver(observer);

        ActionResult result = game.resolveEvents();

        assertTrue(result.isSuccess());
        assertEquals(GamePhase.ENDED, game.getGamePhase());
        assertEquals(1, observer.gameOverCount);
    }

    private static class RecordingObserverPayloads implements ModelObserverPayloads {
        int gameStartedCount;
        int stateChangedCount;
        int endResolvingCount;
        int scoresCount;
        int gameOverCount;

        @Override
        public void onGameStarted() {
            gameStartedCount++;
        }

        @Override
        public void onGameStateChanged(GameState gameState) {
            stateChangedCount++;
        }

        @Override
        public void onEndOfPlacingPhase(GameState gameState) {
        }

        @Override
        public void onEndOfDrawingPhase(GameState gameState) {
        }

        @Override
        public void onExtraDrawRequest(GameState gameState) {
        }

        @Override
        public void onEndOfResolvingPhase(GameState gameState) {
            endResolvingCount++;
        }

        @Override
        public void onEraProgression(GameState gameState) {
        }

        @Override
        public void onGameOver() {
            gameOverCount++;
        }

        @Override
        public void onScores(List<ScoreResult> scoreBoard) {
            scoresCount++;
        }
    }
} */
