package it.polimi.ingsw.am23.model.resolvers;

import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScoreResultTest {

    @Test
    void constructorStoresValues() {
        Player player = new Player("p1", "nick", 1, 2, "red");
        ScoreResult result = new ScoreResult(player, 5, 10);

        assertSame(player, result.player);
        assertEquals(5, result.foodPoints);
        assertEquals(10, result.PP);
    }
}
