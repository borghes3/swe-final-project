package it.polimi.ingsw.am23.model.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void foodAndPrestigeMutationsValidateInputsAndBalances() {
        Player player = new Player("id", "nick", 5, 2, new Totem("id", "red"));

        player.applyFoodDelta(3);
        player.applyFoodDelta(2);
        assertEquals(6, player.getFood());

        assertTrue(player.canAfford(6));
        assertFalse(player.canAfford(7));

        player.addPrestigePoints(4);
        player.spendPrestigePoints(3);
        player.spendPrestigePoints(1);
        assertEquals(2, player.getPrestigePoints());

        assertThrows(IllegalArgumentException.class, () -> player.applyFoodDelta(-1));
        assertThrows(IllegalArgumentException.class, () -> player.applyFoodDelta(100));
        assertThrows(IllegalArgumentException.class, () -> player.canAfford(-1));
        assertThrows(IllegalArgumentException.class, () -> player.addPrestigePoints(-1));
        assertThrows(IllegalArgumentException.class, () -> player.spendPrestigePoints(-1));
        assertThrows(IllegalArgumentException.class, () -> player.spendPrestigePoints(-1));
    }

    @Test
    void getStateReflectsCurrentTribeSnapshot() {
        Player player = new Player("id", "nick", 1, 1, new Totem("id", "red"));
        player.getTribe().addCharacter(new it.polimi.ingsw.am23.model.cards.characters.ArtistCard("a", it.polimi.ingsw.am23.model.enums.Era.ERA_1, 0, 2));

        assertEquals(1, player.getState().getCharacters().size());
    }
}
