package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.cards.characters.HunterCard;
import it.polimi.ingsw.am23.model.effects.HuntingEffectData;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HuntingRewardPerHunterEffectTest {

    @Test
    void applyHuntingAddsBonusFoodAndPointsPerHunter() {
        Player p = TestUtils.player("p", 0, 0);
        p.getTribe().addCharacter(new HunterCard("h1", Era.ERA_1, 0, false, 2));
        p.getTribe().addCharacter(new HunterCard("h2", Era.ERA_1, 0, false, 2));

        HuntingEffectData data = new HuntingEffectData();
        new HuntingRewardPerHunterEffect().applyHunting(null, p, data);

        assertEquals(2, data.getExtraFood());
        assertEquals(2, data.getExtraPoints());
    }
}
