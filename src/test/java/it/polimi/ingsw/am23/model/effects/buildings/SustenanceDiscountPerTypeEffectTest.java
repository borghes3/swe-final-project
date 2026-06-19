package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.cards.characters.HunterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SustenanceDiscountPerTypeEffectTest {

    @Test
    void modifySustenanceCostAppliesDiscountPerCharacterTypeCount() {
        // Input  : tribe with 2 hunters; effect = SustenanceDiscountPerTypeEffect(HUNTER, discountPerType=2);
        //          call modifySustenanceCost(null, p, currentCost=5).
        // Output : 1 (5 - 2 hunters * 2 discount = 5 - 4).
        Player p = TestUtils.player("p", 0, 0);
        p.getTribe().addCharacter(new HunterCard("h1", Era.ERA_1, 0, false, 2));
        p.getTribe().addCharacter(new HunterCard("h2", Era.ERA_1, 0, false, 2));

        SustenanceDiscountPerTypeEffect effect = new SustenanceDiscountPerTypeEffect(CharacterType.HUNTER, 2);
        assertEquals(1, effect.modifySustenanceCost(null, p, 5));
    }
}
