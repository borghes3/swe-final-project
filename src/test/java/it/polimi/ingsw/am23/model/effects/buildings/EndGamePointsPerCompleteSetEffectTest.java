package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.cards.characters.*;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.InventionIcon;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EndGamePointsPerCompleteSetEffectTest {

    @Test
    void getEndGamePointsScalesByCompletedSets() {
        // Input  : tribe contains one of each character type (Hunter+Gatherer+Builder+Shaman+Artist+Inventor) → 1 full set;
        //          effect = EndGamePointsPerCompleteSetEffect(pointsPerSet=4).
        // Output : getEndGamePoints==4.
        Player p = TestUtils.player("p", 0, 0);
        p.getTribe().addCharacter(new HunterCard("h", Era.ERA_1, 0, false, 2));
        p.getTribe().addCharacter(new GathererCard("g", Era.ERA_1, 0, 2));
        p.getTribe().addCharacter(new BuilderCard("b", Era.ERA_1, 0, 1, 2));
        p.getTribe().addCharacter(new ShamanCard("s", Era.ERA_1, 0, 1, 2));
        p.getTribe().addCharacter(new ArtistCard("a", Era.ERA_1, 0, 2));
        p.getTribe().addCharacter(new InventorCard("i", Era.ERA_1, 0, InventionIcon.BOAT, 2));

        assertEquals(4, new EndGamePointsPerCompleteSetEffect(4).getEndGamePoints(null, p));
    }
}
