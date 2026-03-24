package it.polimi.ingsw.am23.model.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void constructorRejectsNullId() {
        assertThrows(NullPointerException.class, () -> new Player(null, "nick", 0, 0, "red"));
    }

    @Test
    void constructorRejectsNullNickname() {
        assertThrows(NullPointerException.class, () -> new Player("id", null, 0, 0, "red"));
    }

    @Test
    void constructorRejectsNegativeFood() {
        assertThrows(IllegalArgumentException.class, () -> new Player("id", "nick", -1, 0, "red"));
    }

    @Test
    void addAndSpendFoodUpdatesBalance() {
        Player player = new Player("id", "nick", 5, 0, "red");
        player.addFood(3);
        assertEquals(8, player.getFood());

        player.spendFood(2);
        assertEquals(6, player.getFood());
    }

    @Test
    void spendFoodThrowsWhenNotEnough() {
        Player player = new Player("id", "nick", 1, 0, "red");
        assertThrows(IllegalArgumentException.class, () -> player.spendFood(2));
    }

    @Test
    void canAffordRejectsNegative() {
        Player player = new Player("id", "nick", 3, 0, "red");
        assertTrue(player.canAfford(3));
        assertFalse(player.canAfford(4));
        assertThrows(IllegalArgumentException.class, () -> player.canAfford(-1));
    }

    @Test
    void addAndSpendPrestigePointsUpdatesBalance() {
        Player player = new Player("id", "nick", 0, 1, "red");
        player.addPrestigePoints(2);
        assertEquals(3, player.getPrestigePoints());

        player.spendPrestigePoints(1);
        assertEquals(2, player.getPrestigePoints());

        player.losePrestigePoints(2);
        assertEquals(0, player.getPrestigePoints());
    }

    @Test
    void negativeAmountsAreRejected() {
        Player player = new Player("id", "nick", 0, 0, "red");
        assertThrows(IllegalArgumentException.class, () -> player.addFood(-1));
        assertThrows(IllegalArgumentException.class, () -> player.spendFood(-1));
        assertThrows(IllegalArgumentException.class, () -> player.addPrestigePoints(-1));
        assertThrows(IllegalArgumentException.class, () -> player.spendPrestigePoints(-1));
        assertThrows(IllegalArgumentException.class, () -> player.losePrestigePoints(-1));
    }
}
