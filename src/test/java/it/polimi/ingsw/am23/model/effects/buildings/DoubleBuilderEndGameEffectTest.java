package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.cards.characters.BuilderCard;
import it.polimi.ingsw.am23.model.cards.characters.HunterCard;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DoubleBuilderEndGameEffectTest {

    @Test
    void getEndGamePointsSumsCharacterPointsInTribe() {
        Player p = TestUtils.player("p", 0, 0);
        p.getTribe().addCharacter(new BuilderCard("b", Era.ERA_1, 3, 1, 2));
        p.getTribe().addCharacter(new HunterCard("h", Era.ERA_1, 2, false, 2));

        assertEquals(5, new DoubleBuilderEndGameEffect().getEndGamePoints(null, p));
    }
}
