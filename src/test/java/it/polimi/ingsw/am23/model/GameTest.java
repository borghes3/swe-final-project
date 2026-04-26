package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.exceptions.PlayerNotFoundException;
import it.polimi.ingsw.am23.model.board.OfferAction;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;
import it.polimi.ingsw.am23.model.cards.events.SustenanceEventCard;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.enums.RowType;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.state.GameState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void startGameMovesToPlacingTotemsAndNotifiesObservers() {
        Player p1 = TestUtils.player("p1", 3, 0);
        Game game = TestUtils.game(
                List.of(p1),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, 0,"p1")),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );
        RecordingObserver observer = new RecordingObserver();
        game.addObserver(observer);

        game.startGame();

        assertEquals(GamePhase.PLACING_TOTEMS, game.getGamePhase());
        assertEquals(1, observer.gameStartedCount);
        assertEquals(0, observer.stateChangedCount);
    }

    @Test
    void findPlayerThrowsForMissingPlayer() {
        Player p1 = TestUtils.player("p1", 3, 0);
        Game game = TestUtils.game(
                List.of(p1),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, 0, null)),
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
                        new TurnOrderSlot(0, 0,"p1"),
                        new TurnOrderSlot(0, 0,"p2")
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
                        new TurnOrderSlot(0, 0, "p1"),
                        new TurnOrderSlot(0, 0,"p2")
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
    void takeCardRejectsWronRow() {
        Player p1 = TestUtils.player("p1", 3, 0);
        Player p2 = TestUtils.player("p2", 3, 0);

        Game game = TestUtils.game(
                List.of(p1, p2),
                List.of(new OfferTile('A', "p1", 2, new OfferAction(1, 0, 0))),
                List.of(new TurnOrderSlot(0, 0, null), new TurnOrderSlot(0, 0, "p2")),
                List.of(TestUtils.artist("a1", Era.ERA_1)),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        ActionResult result = game.takeSingleCard("p1", new SelectedSingleCard(RowType.BOTTOM, 0, false));
        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.INVALID_ROW, result.getError());

    }

    @Test
    void takeCardsRejectsEventCardsAndDoesNotConsumeTurn() {
        Player p1 = TestUtils.player("p1", 3, 0);
        Player p2 = TestUtils.player("p2", 3, 0);

        Card topEvent = new SustenanceEventCard("se1", Era.ERA_1, 0, false);
        Game game = TestUtils.game(
                List.of(p1, p2),
                List.of(new OfferTile('A', "p1", 2, new OfferAction(1, 0, 0))),
                List.of(new TurnOrderSlot(0, 0,null), new TurnOrderSlot(0, 0, "p2")),
            List.of(topEvent, TestUtils.artist("a1", Era.ERA_1)),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        ActionResult result = game.takeSingleCard("p1", new SelectedSingleCard(RowType.TOP, 0 , false));

        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.CARD_NOT_TAKABLE, result.getError());
        assertNotNull(game.getBoard().getOfferTileByPlayerId("p1"));
    }

    @Test
    void takeCardsSuccessReturnsPlayerToTurnOrderAndGrantsFoodReward() {
        Player p1 = TestUtils.player("p1", 1, 0);
        Player p2 = TestUtils.player("p2", 0, 0);

        Game game = TestUtils.game(
                List.of(p1, p2),
                List.of(new OfferTile('A', "p1", 2, new OfferAction(1, 0, 2))),
                List.of(new TurnOrderSlot(1, 0, null), new TurnOrderSlot(0, 0, "p2")),
                List.of(TestUtils.artist("a1", Era.ERA_1)),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        ActionResult result = game.takeSingleCard("p1", new SelectedSingleCard(RowType.TOP, 0, false));

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
                List.of(new TurnOrderSlot(0, 0,null), new TurnOrderSlot(0, 0,null)),
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
    void resolveEventsAndCalculateScoresProduceObserverNotifications() {
        Player p1 = TestUtils.player("p1", 3, 0);
        Player p2 = TestUtils.player("p2", 3, 0);
        Game game = TestUtils.game(
                List.of(p1, p2),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, 0,null), new TurnOrderSlot(0, 0,null)),
                List.of(),
                List.of(new SustenanceEventCard("s", Era.ERA_1, 0, false)),
                List.of(),
                Era.ERA_1,
                1
        );

        RecordingObserver observer = new RecordingObserver();
        game.addObserver(observer);

        ActionResult eventsResult = game.resolveEvents();
        ActionResult scoresResult = game.calculateScores();

        assertTrue(eventsResult.isSuccess());
        assertTrue(scoresResult.isSuccess());
        assertEquals(1, observer.eventResolvedCount);
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
                List.of(new TurnOrderSlot(0, 0,null), new TurnOrderSlot(0, 0,null)),
                List.of(new SustenanceEventCard("s-top", Era.ERA_1, 0, true)),
                List.of(new SustenanceEventCard("s-bottom", Era.ERA_1, 0, false)),
                List.of(),
                Era.ERA_1,
                10
        );
        RecordingObserver observer = new RecordingObserver();
        game.addObserver(observer);

        ActionResult result = game.resolveEvents();

        assertTrue(result.isSuccess());
        assertEquals(GamePhase.ENDED, game.getGamePhase());
        assertEquals(1, observer.gameOverCount);
    }

    private static class RecordingObserver implements ModelObserver {
        int gameStartedCount;
        int stateChangedCount;
        int endPlacingCount;
        int endDrawingCount;
        int extraDrawCount;
        int eventResolvedCount;
        int eraProgressionCount;
        int scoresCount;
        int gameOverCount;
        GameState lastGameState;

        @Override
        public void onGameStarted(GameState gameState) {
            gameStartedCount++;
            lastGameState = gameState;
        }

        @Override
        public void onGameStateChanged(GameState gameState) {
            stateChangedCount++;
            lastGameState = gameState;
        }

        @Override
        public void onEndOfPlacingPhase(GameState gameState) {
            endPlacingCount++;
            lastGameState = gameState;
        }

        @Override
        public void onEndOfDrawingPhase(GameState gameState) {
            endDrawingCount++;
            lastGameState = gameState;
        }

        @Override
        public void onExtraDrawRequest(GameState gameState) {
            extraDrawCount ++;
            lastGameState = gameState;
        }

        @Override
        public void onEventResolved(GameState gameState) {
            eventResolvedCount++;
            lastGameState = gameState;
        }

        @Override
        public void onEraProgression(GameState gameState) {
            eraProgressionCount++;
            lastGameState = gameState;
        }

        @Override
        public void onGameOver(GameState gameState) {
            gameOverCount++;
            lastGameState = gameState;
        }

        @Override
        public void onScoreboardAvailable(GameState gameState) {
            scoresCount++;
            lastGameState = gameState;
        }
    }
}
