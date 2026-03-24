package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HunterCardTest {

    @Test
    void onTakenAddsFoodBasedOnHuntersInTribe() {
        Player player = new Player("p1", "nick", 0, 0, "red");
        Game game = new Game();

        HunterCard first = new HunterCard("h1", Era.ERA_1, 0, true);
        first.onTaken(game, player);
        assertEquals(1, player.getFood());

        HunterCard second = new HunterCard("h2", Era.ERA_1, 0, true);
        second.onTaken(game, player);
        assertEquals(3, player.getFood());
    }

    @Test
    void onTakenWithNoFoodSymbolDoesNotAddFood() {
        Player player = new Player("p1", "nick", 0, 0, "red");
        Game game = new Game();

        HunterCard hunter = new HunterCard("h1", Era.ERA_1, 0, false);
        hunter.onTaken(game, player);

        assertEquals(0, player.getFood());
    }
}
