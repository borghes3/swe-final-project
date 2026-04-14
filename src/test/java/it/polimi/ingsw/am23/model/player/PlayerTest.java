package it.polimi.ingsw.am23.model.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void foodAndPrestigeMutationsValidateInputsAndBalances() {
        Player player = new Player("id", "nick", 5, 2, new Totem("id", "red"));

        player.addFood(3);
        player.spendFood(2);
        assertEquals(6, player.getFood());

        assertTrue(player.canAfford(6));
        assertFalse(player.canAfford(7));

        player.addPrestigePoints(4);
        player.spendPrestigePoints(3);
        player.losePrestigePoints(1);
        assertEquals(2, player.getPrestigePoints());

        assertThrows(IllegalArgumentException.class, () -> player.addFood(-1));
        assertThrows(IllegalArgumentException.class, () -> player.spendFood(100));
        assertThrows(IllegalArgumentException.class, () -> player.canAfford(-1));
        assertThrows(IllegalArgumentException.class, () -> player.addPrestigePoints(-1));
        assertThrows(IllegalArgumentException.class, () -> player.spendPrestigePoints(-1));
        assertThrows(IllegalArgumentException.class, () -> player.losePrestigePoints(-1));
    }

    @Test
    void getStateReflectsCurrentTribeSnapshot() {
        Player player = new Player("id", "nick", 1, 1, new Totem("id", "red"));
        player.getTribe().addCharacter(new it.polimi.ingsw.am23.model.cards.characters.ArtistCard("a", it.polimi.ingsw.am23.model.enums.Era.ERA_1, 0, 2));

        assertEquals(1, player.getState().getCharacters().size());
    }
}
