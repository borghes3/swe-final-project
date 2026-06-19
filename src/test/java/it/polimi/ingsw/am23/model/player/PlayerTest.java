package it.polimi.ingsw.am23.model.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void foodAndPrestigeMutationsValidateInputsAndBalances() {
        // Input  : Player(food=5, PP=2); apply food deltas (+3, -2); query canAfford for 6, -6, 7;
        //          add 4 PP, spend 3 PP and -1 PP; then try food -7 and addPrestige(-1).
        // Output : after deltas food==6; canAfford 6/-6 are true, canAfford 7 is false;
        //          after PP ops prestige==2 (2+4-3+1); food delta -7 and addPrestige(-1) throw IllegalArgumentException.
        Player player = new Player("id", "nick", 5, 2, new Totem("id", "red"));

        player.applyFoodDelta(3);
        player.applyFoodDelta(-2);
        assertEquals(6, player.getFood());

        assertTrue(player.canAfford(6));
        assertTrue(player.canAfford(-6));
        assertFalse(player.canAfford(7));

        player.addPrestigePoints(4);
        player.spendPrestigePoints(3);
        player.spendPrestigePoints(-1);
        assertEquals(2, player.getPrestigePoints());

        assertThrows(IllegalArgumentException.class, () -> player.applyFoodDelta(-7));
        assertThrows(IllegalArgumentException.class, () -> player.addPrestigePoints(-1));
    }

    @Test
    void getStateReflectsCurrentTribeSnapshot() {
        // Input  : Player with 1 artist added to tribe; call getState().
        // Output : player.getState().getCharacters().size()==1 (reflects the artist).
        Player player = new Player("id", "nick", 1, 1, new Totem("id", "red"));
        player.getTribe().addCharacter(new it.polimi.ingsw.am23.model.cards.characters.ArtistCard("a", it.polimi.ingsw.am23.model.enums.Era.ERA_1, 0, 2));

        assertEquals(1, player.getState().getCharacters().size());
    }
}
