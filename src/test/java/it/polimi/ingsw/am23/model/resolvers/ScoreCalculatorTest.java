package it.polimi.ingsw.am23.model.resolvers;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.board.OfferAction;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.cards.characters.ArtistCard;
import it.polimi.ingsw.am23.model.cards.characters.BuilderCard;
import it.polimi.ingsw.am23.model.cards.characters.InventorCard;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.InventionIcon;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoreCalculatorTest {

    @Test
    void calculateFinalScoresSortsByPrestigeThenFoodOnTie() {
        // Input  : p1(food=1,PP=4)+scoringSet, p2(food=5,PP=10), p3(food=3,PP=4)+scoringSet.
        //          Each scoringSet adds 22 PP from buildings/cards (one inventor pair, builder, artists, +3 building).
        // Output : final ranking [p3, p1, p2] — p3/p1 tied at PP=26 but p3 has more food breaking tie; p2 has 10.
        Player p1 = TestUtils.player("p1", 1, 4);
        Player p2 = TestUtils.player("p2", 5, 10);
        Player p3 = TestUtils.player("p3", 3, 4);

        Game game = minimalGame(List.of(p1, p2, p3));

        addScoringSet(game, p1);
        addScoringSet(game, p3);

        ScoreCalculator calculator = new ScoreCalculator(game);
        List<ScoreResult> scores = calculator.calculateFinalScores();

        assertEquals(List.of("p3", "p1", "p2"), scores.stream().map(s -> s.player.getId()).toList());
        assertEquals(26, scores.get(0).PP);
        assertEquals(26, scores.get(1).PP);
        assertEquals(10, scores.get(2).PP);
    }

    @Test
    void getWinnerThrowsIfScoresWereNeverCalculatedAndReturnsTieWinners() {
        // Input  : 3 players, no scoring sets — p1/p2 have (food=2,PP=5), p3 has (food=1,PP=5).
        //          Call getWinner() before calculateFinalScores(), then after.
        // Output : first call throws IllegalStateException; after calculate, winners=[p1, p2] (tied on PP+food).
        Player p1 = TestUtils.player("p1", 2, 5);
        Player p2 = TestUtils.player("p2", 2, 5);
        Player p3 = TestUtils.player("p3", 1, 5);

        ScoreCalculator calculator = new ScoreCalculator(minimalGame(List.of(p1, p2, p3)));
        assertThrows(IllegalStateException.class, calculator::getWinner);

        calculator.calculateFinalScores();
        List<Player> winners = calculator.getWinner();

        assertEquals(2, winners.size());
        assertTrue(winners.stream().anyMatch(p -> p.getId().equals("p1")));
        assertTrue(winners.stream().anyMatch(p -> p.getId().equals("p2")));
    }

    private static void addScoringSet(Game game, Player player) {
        player.getTribe().addCharacter(new BuilderCard("b1", Era.ERA_1, 3, 0, 2));
        new InventorCard("i1", Era.ERA_1, 0, InventionIcon.BOAT, 2).onTaken(game, player);
        new InventorCard("i2", Era.ERA_1, 0, InventionIcon.ARROW, 2).onTaken(game, player);
        player.getTribe().addCharacter(new ArtistCard("a1", Era.ERA_1, 0, 2));
        player.getTribe().addCharacter(new ArtistCard("a2", Era.ERA_1, 0, 2));
        player.getTribe().addBuilding(TestUtils.building("bd1", Era.ERA_1, 2, 0, new FlatEndGamePointsEffect(3)));
    }

    private static Game minimalGame(List<Player> players) {
        return TestUtils.game(
                players,
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, 0,null)),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );
    }
}
