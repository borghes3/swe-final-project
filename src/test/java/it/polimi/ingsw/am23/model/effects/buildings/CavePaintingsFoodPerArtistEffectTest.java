package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.cards.characters.ArtistCard;
import it.polimi.ingsw.am23.model.effects.CavePaintingsEffectData;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CavePaintingsFoodPerArtistEffectTest {

    @Test
    void applyCavePaintingsAddsFoodPerArtist() {
        // Input  : Player p with 2 artists; effect = CavePaintingsFoodPerArtistEffect(foodPerArtist=2).
        //          applyCavePaintings(null, p, data) — 2 artists * 2 food each.
        // Output : data.getExtraFood()==4.
        Player p = TestUtils.player("p", 0, 0);
        p.getTribe().addCharacter(new ArtistCard("a1", Era.ERA_1, 0, 2));
        p.getTribe().addCharacter(new ArtistCard("a2", Era.ERA_1, 0, 2));

        CavePaintingsEffectData data = new CavePaintingsEffectData();
        new CavePaintingsFoodPerArtistEffect(2).applyCavePaintings(null, p, data);

        assertEquals(4, data.getExtraFood());
    }
}
