package it.polimi.ingsw.am23.model.resolvers;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.characters.ArtistCard;
import it.polimi.ingsw.am23.model.cards.characters.BuilderCard;
import it.polimi.ingsw.am23.model.cards.characters.InventorCard;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.InventionIcon;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoreCalculatorTest {

    @Test
    void calculateFinalScoresSortsByPointsThenFood() throws Exception {
        Player p1 = new Player("p1", "nick1", 1, 5, "red");
        Player p2 = new Player("p2", "nick2", 3, 5, "blue");
        Player p3 = new Player("p3", "nick3", 0, 0, "green");

        addScoringCards(p1);
        addScoringCards(p2);

        Game game = gameWithPlayers(List.of(p1, p2, p3));
        ScoreCalculator calculator = new ScoreCalculator(game);

        List<ScoreResult> results = calculator.calculateFinalScores();

        for (ScoreResult r : results) {
            System.out.println(r.player.getId() + " -> PP: " + r.PP + ", food: " + r.foodPoints);
        }

        assertEquals(3, results.size());
        assertEquals("p2", results.get(0).player.getId());
        assertEquals("p1", results.get(1).player.getId());
        assertEquals("p3", results.get(2).player.getId());
        assertEquals(3, results.get(0).foodPoints);
    }

    @Test
    void getWinnerThrowsIfScoresNotCalculated() {
        Game game = new Game();
        ScoreCalculator calculator = new ScoreCalculator(game);
        assertThrows(IllegalStateException.class, calculator::getWinner);
    }

    @Test
    void getWinnerReturnsMultipleOnTie() throws Exception {
        Player p1 = new Player("p1", "nick1", 2, 5, "red");
        Player p2 = new Player("p2", "nick2", 2, 5, "blue");

        addScoringCards(p1);
        addScoringCards(p2);

        Game game = gameWithPlayers(List.of(p1, p2));
        ScoreCalculator calculator = new ScoreCalculator(game);

        calculator.calculateFinalScores();
        List<Player> winners = calculator.getWinner();

        assertEquals(2, winners.size());
        assertTrue(winners.stream().anyMatch(p -> p.getId().equals("p1")));
        assertTrue(winners.stream().anyMatch(p -> p.getId().equals("p2")));
    }

    private static void addScoringCards(Player player) {
        player.getTribe().addCharacter(new BuilderCard("b1", Era.ERA_1, 3, 0));
        player.getTribe().addCharacter(new InventorCard("i1", Era.ERA_1, 0, InventionIcon.BOAT));
        player.getTribe().addCharacter(new InventorCard("i2", Era.ERA_1, 0, InventionIcon.ARROW));
        player.getTribe().addCharacter(new ArtistCard("a1", Era.ERA_1, 0));
        player.getTribe().addCharacter(new ArtistCard("a2", Era.ERA_1, 0));
        player.getTribe().addBuilding(building("bd1", 2, 1));
    }

    private static BuildingCard building(String id, int points, int endGameBonus) {
        return new BuildingCard(id, Era.ERA_1, points, 0, new BuildingEffect() {
            @Override
            public int getEndGamePoints(Game game, Player player) {
                return endGameBonus;
            }
        });
    }

    private static Game gameWithPlayers(List<Player> players) throws Exception {
        Game game = new Game();
        Field field = Game.class.getDeclaredField("players");
        field.setAccessible(true);
        field.set(game, players);
        return game;
    }
}
