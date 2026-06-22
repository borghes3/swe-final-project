package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.exceptions.PlayerNotFoundException;
import it.polimi.ingsw.am23.model.board.Board;
import it.polimi.ingsw.am23.model.board.CardMarket;
import it.polimi.ingsw.am23.model.board.OfferAction;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.deck.BuildingDeck;
import it.polimi.ingsw.am23.model.deck.TribeDeck;
import it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;
import it.polimi.ingsw.am23.model.cards.events.SustenanceEventCard;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.enums.RowType;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.resolvers.EventResolver;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void startGameMovesToPlacingTotemsAndNotifiesObservers() {
        // Input  : single player p1, one offer tile, one turn-order slot; attach observer; call startGame().
        // Output : phase==PLACING_TOTEMS; observer received 1 onGameStarted, no extra state-changed events.
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
        TestUtils.RecordingObserver observer = new TestUtils.RecordingObserver();
        game.addObserver(observer);

        game.startGame();

        assertEquals(GamePhase.PLACING_TOTEMS, game.getGamePhase());
        assertEquals(1, observer.gameStartedCount);
        assertEquals(0, observer.stateChangedCount);
    }

    @Test
    void findPlayerThrowsForMissingPlayer() {
        // Input  : game with only player "p1"; call findPlayer("missing").
        // Output : PlayerNotFoundException is thrown.
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
        // Input  : turn-order with p1 first then p2; call game.placeTotem("p2", 'A') (wrong order).
        // Output : failure with ErrorCode.WRONG_PLAYER, offer tile 'A' remains free, turn-order still has p1 first.
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
        // Input  : two players p1/p2 in turn order; both call placeTotem in sequence ('A' then 'B').
        // Output : both placements succeed, after the last one phase==RESOLVING_OFFERS,
        //          and the turn-order tile is empty (no more pending totems).
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
    void placeTotemRejectsOccupiedOfferTile() {
        // Input  : offer 'A' already occupied by another player; p1 (whose turn it is) calls placeTotem("p1", 'A').
        // Output : failure with ErrorCode.INVALID_TILE.
        Player p1 = TestUtils.player("p1", 3, 0);

        Game game = TestUtils.game(
                List.of(p1),
                List.of(new OfferTile('A', "other", 2, new OfferAction(1, 0, 0))),
                List.of(new TurnOrderSlot(0, 0, "p1")),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        ActionResult result = game.placeTotem("p1", 'A');

        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.INVALID_TILE, result.getError());
    }

    @Test
    void takeCardRejectsWronRow() {
        // Input  : offer 'A' grants 1 TOP draw (bottom=0); p1 calls takeSingleCard with BOTTOM row.
        // Output : failure with ErrorCode.INVALID_ROW (drawable count from BOTTOM is 0).
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
        TestUtils.setPhase(game, GamePhase.RESOLVING_OFFERS);

        ActionResult result = game.takeSingleCard("p1", new SelectedSingleCard(RowType.BOTTOM, 0, false));
        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.INVALID_ROW, result.getError());

    }

    @Test
    void takeCardsRejectsEventCardsAndDoesNotConsumeTurn() {
        // Input  : Market TOP=[event sustenance card, artist a1]; p1 calls takeSingleCard for TOP[0] (the event).
        // Output : failure with ErrorCode.CARD_NOT_TAKABLE, p1 still has the offer tile (turn not consumed).
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
        TestUtils.setPhase(game, GamePhase.RESOLVING_OFFERS);

        ActionResult result = game.takeSingleCard("p1", new SelectedSingleCard(RowType.TOP, 0 , false));

        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.CARD_NOT_TAKABLE, result.getError());
        assertNotNull(game.getBoard().getOfferTileByPlayerId("p1"));
    }

    @Test
    void takeCardsSuccessReturnsPlayerToTurnOrderAndGrantsFoodReward() {
        // Input  : p1(food=1) on offer 'A' with foodReward=2 and 1 TOP draw; takes artist a1.
        // Output : success; p1.getFood()==3 (1+2 reward), tribe gets 1 character, p1 placed back on turn-order slot 0.
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
        TestUtils.setPhase(game, GamePhase.RESOLVING_OFFERS);

        ActionResult result = game.takeSingleCard("p1", new SelectedSingleCard(RowType.TOP, 0, false));

        assertTrue(result.isSuccess());
        assertEquals(3, p1.getFood());
        assertEquals(1, p1.getTribe().getCharacters().size());
        assertEquals("p1", game.getBoard().getTurnOrderTile().getSlot(0).getPlayerId());
    }

    @Test
    void takeSingleCardAcceptsBuildingCardsAndPaysFoodCost() {
        // Input  : p1(food=5), top building "b1" with cost=3; call takeSingleCard(TOP, 0, isBuilding=true).
        // Output : success; p1.getFood()==2 (5-3), tribe has 1 building, p1 is placed in turn-order slot 0.
        Player p1 = TestUtils.player("p1", 5, 0);

        Game game = TestUtils.game(
                List.of(p1),
                List.of(new OfferTile('A', "p1", 2, new OfferAction(1, 0, 0))),
                List.of(new TurnOrderSlot(0, 0, null)),
                List.of(),
                List.of(),
                List.of(TestUtils.building("b1", Era.ERA_1, 0, 3, new it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect(0))),
                Era.ERA_1,
                1
        );
        TestUtils.setPhase(game, GamePhase.RESOLVING_OFFERS);

        ActionResult result = game.takeSingleCard("p1", new SelectedSingleCard(RowType.TOP, 0, true));

        System.out.println(result.getError());

        assertTrue(result.isSuccess());
        assertEquals(2, p1.getFood());
        assertEquals(1, p1.getTribe().getBuildings().size());
        assertEquals("p1", game.getBoard().getTurnOrderTile().getSlot(0).getPlayerId());
    }

    @Test
    void takeSingleCardAppliesPositiveTurnOrderFoodDelta() {
        // Input  : p1(food=1), offer 'A' with no food reward but turn-order slot has +2 food delta.
        //          Take artist a1 → after drawing player is moved back to the slot (+2 food).
        // Output : p1.getFood()==3 (1 + 2 from turn-order slot).
        Player p1 = TestUtils.player("p1", 1, 0);

        Game game = TestUtils.game(
                List.of(p1),
                List.of(new OfferTile('A', "p1", 1, new OfferAction(1, 0, 0))),
                List.of(new TurnOrderSlot(0, 2, null)),
                List.of(TestUtils.artist("a1", Era.ERA_1)),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );
        TestUtils.setPhase(game, GamePhase.RESOLVING_OFFERS);

        ActionResult result = game.takeSingleCard("p1", new SelectedSingleCard(RowType.TOP, 0, false));

        assertTrue(result.isSuccess());
        assertEquals(3, p1.getFood());
    }

    @Test
    void takeSingleCardFallsBackToPrestigeWhenTurnOrderFoodIsTooExpensive() {
        // Input  : p1(food=1, PP=10); turn-order slot delta = -2 food. p1 cannot afford 2 food.
        //          Take artist; on return-to-slot the negative delta is paid in PP (2*delta) instead of food.
        // Output : p1.getFood()==1 (unchanged), p1.getPrestigePoints()==6 (10 - 4).
        Player p1 = TestUtils.player("p1", 1, 10);

        Game game = TestUtils.game(
                List.of(p1),
                List.of(new OfferTile('A', "p1", 1, new OfferAction(1, 0, 0))),
                List.of(new TurnOrderSlot(0, -2, null)),
                List.of(TestUtils.artist("a1", Era.ERA_1)),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );
        TestUtils.setPhase(game, GamePhase.RESOLVING_OFFERS);

        ActionResult result = game.takeSingleCard("p1", new SelectedSingleCard(RowType.TOP, 0, false));

        assertTrue(result.isSuccess());
        assertEquals(1, p1.getFood());
        assertEquals(6, p1.getPrestigePoints());
    }

    @Test
    void takeExtraCardValidatesPendingPlayerAndCost() {
        // Input  : 3 sequential takeExtraCard calls — (a) before any pending player is set;
        //          (b) after setting p1 as pending, p2 tries to draw; (c) p1 tries to buy a building
        //          costing 4 food but only has 1.
        // Output : (a) NO_PENDING_EXTRA_DRAW, (b) INVALID_EXTRA_DRAW, (c) NOT_ENOUGH_FOOD.
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
    void takeExtraCardAcceptsTribeCardSelection() {
        // Input  : p1 is set as pending extra-draw player; p1 picks TOP[0] (a tribe artist) as extra card.
        // Output : success; p1's tribe gains 1 character; phase advances to RESOLVING_EVENTS.
        Player p1 = TestUtils.player("p1", 2, 0);

        Game game = TestUtils.game(
                List.of(p1),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, 0, null)),
                List.of(TestUtils.artist("a1", Era.ERA_1)),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        game.setPendingExtraDrawPlayerId("p1");

        ActionResult result = game.takeExtraCard("p1", new SelectedCardExtraDraw(0, null));

        assertTrue(result.isSuccess());
        assertEquals(1, p1.getTribe().getCharacters().size());
        assertEquals(GamePhase.RESOLVING_EVENTS, game.getGamePhase());
    }

    @Test
    void takeExtraCardAcceptsBuildingSelection() {
        // Input  : p1(food=5) is pending extra-draw; picks the top building "b1" (cost=2) as extra card.
        // Output : success; p1.getFood()==3 (5-2); 1 building added to tribe; phase==RESOLVING_EVENTS.
        Player p1 = TestUtils.player("p1", 5, 0);

        Game game = TestUtils.game(
                List.of(p1),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, 0, null)),
                List.of(),
                List.of(),
                List.of(TestUtils.building("b1", Era.ERA_1, 0, 2, new it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect(0))),
                Era.ERA_1,
                1
        );

        game.setPendingExtraDrawPlayerId("p1");

        ActionResult result = game.takeExtraCard("p1", new SelectedCardExtraDraw(null, 0));

        assertTrue(result.isSuccess());
        assertEquals(3, p1.getFood());
        assertEquals(1, p1.getTribe().getBuildings().size());
        assertEquals(GamePhase.RESOLVING_EVENTS, game.getGamePhase());
    }

    @Test
    void resolveEventsAndCalculateScoresProduceObserverNotifications() {
        // Input  : 2 players; BOTTOM has 1 sustenance event card; round=1; attach observer;
        //          call resolveEvents() and calculateScores(); then removeObserver and calculateScores() again.
        // Output : both ActionResults succeed; observer recorded 1 onEventResolved and 1 onScoreboardAvailable;
        //          after removeObserver, scoresCount stays at 1.
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

        TestUtils.RecordingObserver observer = new TestUtils.RecordingObserver();
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
        void resolveEventsAdvancesEraDuringCleanup() {
        // Input  : 2 players; BOTTOM has 1 ERA_1 event; tribe deck contains an ERA_2 artist + several ERA_1 fillers;
        //         resolveEvents() triggers cleanup which refills TOP and discovers an ERA_2 card → era progression.
        // Output : success; currentEra==ERA_2; phase==PLACING_TOTEMS; observer recorded 1 onEraProgression.
        Player p1 = TestUtils.player("p1", 3, 0);
            Player p2 = TestUtils.player("p2", 3, 0);

        Board board = new Board(
            List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                new it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderTile(List.of(new TurnOrderSlot(0, 0, null), new TurnOrderSlot(1, 0, null)))
        );

        CardMarket cardMarket = new CardMarket(
            List.of(),
            List.of(new SustenanceEventCard("e1", Era.ERA_1, 0, false)),
            List.of()
        );

        Map<Era, List<BuildingCard>> buildingsByEra = new EnumMap<>(Era.class);
        buildingsByEra.put(Era.ERA_2, List.of(TestUtils.building("b2", Era.ERA_2, 0, 0, new it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect(0))));

        Game game = new Game(
            List.of(p1, p2),
            board,
            new TribeDeck(List.of(
                TestUtils.artist("a2", Era.ERA_2),
                TestUtils.artist("a3", Era.ERA_1),
                TestUtils.artist("a4", Era.ERA_1),
                TestUtils.artist("a5", Era.ERA_1),
                TestUtils.artist("a6", Era.ERA_1),
                TestUtils.artist("a7", Era.ERA_1)
            )),
            new BuildingDeck(buildingsByEra),
            new EventResolver(),
            cardMarket,
            Era.ERA_1,
            1
        );

        TestUtils.RecordingObserver observer = new TestUtils.RecordingObserver();
        game.addObserver(observer);

        ActionResult result = game.resolveEvents();

        assertTrue(result.isSuccess());
        assertEquals(Era.ERA_2, game.getCurrentEra());
        assertEquals(GamePhase.PLACING_TOTEMS, game.getGamePhase());
        assertEquals(1, observer.eraProgressionCount);
    }

    @Test
    void resolveEventsAtRoundTenEndsGameAndNotifiesGameOver() {
        // Input  : currentRound=10 (last round); TOP and BOTTOM each contain a sustenance event; call resolveEvents().
        // Output : success; phase==ENDED; observer recorded 1 onGameOver.
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
        TestUtils.RecordingObserver observer = new TestUtils.RecordingObserver();
        game.addObserver(observer);

        ActionResult result = game.resolveEvents();

        assertTrue(result.isSuccess());
        assertEquals(GamePhase.ENDED, game.getGamePhase());
        assertEquals(1, observer.gameOverCount);
    }
    @Test
    void autoResolveProcessesZeroDrawTilesAutomatically() {
        // Input  : offer 'A' = (0 draws, foodReward=3), offer 'B' = (1 TOP draw, no reward).
        //          Both players place totems (p1→'A', p2→'B'). The 'A' tile has 0 draws → auto-resolved.
        // Output : phase==RESOLVING_OFFERS; 'A' freed automatically; p1 returned to turn order with +3 food;
        //          'B' remains occupied by p2 (still needs to draw).
        Player p1 = TestUtils.player("p1", 0, 0);
        Player p2 = TestUtils.player("p2", 0, 0);
        Game game = TestUtils.game(
                List.of(p1, p2),
                List.of(
                        new OfferTile('A', null, 2, new OfferAction(0, 0, 3)),
                        new OfferTile('B', null, 2, new OfferAction(1, 0, 0))
                ),
                List.of(new TurnOrderSlot(0, 0, "p1"), new TurnOrderSlot(1, 0, "p2")),
                List.of(TestUtils.artist("a1", Era.ERA_1)),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        game.startGame();

        game.placeTotem("p1", 'A');
        game.placeTotem("p2", 'B');

        assertEquals(GamePhase.RESOLVING_OFFERS, game.getGamePhase());

        assertTrue(game.getBoard().getOfferTile('A').isFree());
        assertNotNull(game.getBoard().findTurnOrderSlotOccupiedBy("p1"));
        assertEquals(3, p1.getFood());
        assertEquals("p2", game.getBoard().getOfferTile('B').getOccupiedByPlayerId());
    }

    @Test
    void skipTurnFailsWhenCharacterCardIsAvailable() {
        // Input  : single player p1, offer 'A' grants 1 TOP draw, market TOP has 1 artist (a character) available.
        //          After placeTotem, p1 calls skipTurn (but a character is available, so skipping is forbidden).
        // Output : failure with ErrorCode.CANNOT_SKIP.
        Player p1 = TestUtils.player("p1", 0, 0);

        Game game = TestUtils.game(
                List.of(p1),
                List.of(new OfferTile('A', null, 1, new OfferAction(1, 0, 0))),
                List.of(new TurnOrderSlot(0, 0, "p1")),
                List.of(TestUtils.artist("a1", Era.ERA_1)),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        game.startGame();
        game.placeTotem("p1", 'A');

        ActionResult result = game.skipTurn("p1");

        assertFalse(result.isSuccess());
        assertEquals(ErrorCode.CANNOT_SKIP, result.getError());
    }

    @Test
    void skipTurnSucceedsWhenOnlyBuildingsAvailable() {
        // Input  : p1 (food=0) places totem on offer 'A' that grants 2 top draws.
        //          Market = [TOP=[artist a1], BOTTOM=[], top buildings=[b1]].
        //          p1 takes the artist (1 of 2 top draws), then calls skipTurn("p1").
        // Output : skipTurn succeeds (only buildings remain on TOP — no character forced),
        //          offer 'A' is freed, and the game advances to RESOLVING_EVENTS.
        Player p1 = TestUtils.player("p1", 0, 0);

        Game game = TestUtils.game(
                List.of(p1),
                List.of(new OfferTile('A', null, 1, new OfferAction(2, 0, 0))),
                List.of(new TurnOrderSlot(0, 0, "p1")),
                List.of(TestUtils.artist("a1", Era.ERA_1)),
                List.of(),
                List.of(TestUtils.building("b1", Era.ERA_1, 0, 0, new FlatEndGamePointsEffect(0))),
                Era.ERA_1,
                1
        );

        game.startGame();
        game.placeTotem("p1", 'A');
        assertTrue(game.takeSingleCard("p1", new SelectedSingleCard(RowType.TOP, 0, false)).isSuccess());

        ActionResult result = game.skipTurn("p1");

        assertTrue(result.isSuccess());
        assertTrue(game.getBoard().getOfferTile('A').isFree());
        assertEquals(GamePhase.RESOLVING_EVENTS, game.getGamePhase());
    }

    @Test
    void skipTurnSucceedsInExtraDrawPhase() {
        // Input  : p1 places totem on 'A' (1 TOP draw); pending extra-draw is set to p1;
        //          p1 takes the only top building (consuming the draw and entering EXTRA_DRAW phase);
        //          then p1 calls skipTurn (no characters available → skip allowed).
        // Output : skipTurn succeeds; phase advances to RESOLVING_EVENTS; currentPlayerId becomes null.
        Player p1 = TestUtils.player("p1", 0, 0);

        Game game = TestUtils.game(
                List.of(p1),
                List.of(new OfferTile('A', null, 1, new OfferAction(1, 0, 0))),
                List.of(new TurnOrderSlot(0, 0, "p1")),
                List.of(),
                List.of(),
                List.of(TestUtils.building("b1", Era.ERA_1, 0, 0, new it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect(0))),
                Era.ERA_1,
                1
        );

        game.startGame();
        game.placeTotem("p1", 'A');

        game.setPendingExtraDrawPlayerId("p1");

        game.takeSingleCard("p1", new SelectedSingleCard(RowType.TOP, 0, true));

        assertEquals(GamePhase.EXTRA_DRAW, game.getGamePhase());

        ActionResult result = game.skipTurn("p1");

        assertTrue(result.isSuccess());
        assertEquals(GamePhase.RESOLVING_EVENTS, game.getGamePhase());
        assertNull(game.getGameState().getCurrentPlayerId());
    }

}
