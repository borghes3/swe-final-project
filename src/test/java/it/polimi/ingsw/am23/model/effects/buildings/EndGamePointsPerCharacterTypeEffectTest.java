package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.cards.characters.HunterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EndGamePointsPerCharacterTypeEffectTest {

    @Test
    void getEndGamePointsScalesByCharacterCount() {
        // Input  : tribe with 2 hunters; effect = EndGamePointsPerCharacterTypeEffect(HUNTER, pointsPer=3).
        // Output : getEndGamePoints==6 (2 hunters * 3 points each).
        Player p = TestUtils.player("p", 0, 0);
        p.getTribe().addCharacter(new HunterCard("h1", Era.ERA_1, 0, false, 2));
        p.getTribe().addCharacter(new HunterCard("h2", Era.ERA_1, 0, false, 2));

        EndGamePointsPerCharacterTypeEffect effect = new EndGamePointsPerCharacterTypeEffect(CharacterType.HUNTER, 3);
        assertEquals(6, effect.getEndGamePoints(null, p));
    }
}
