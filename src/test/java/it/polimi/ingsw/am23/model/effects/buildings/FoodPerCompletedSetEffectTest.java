package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.cards.characters.*;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.InventionIcon;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FoodPerCompletedSetEffectTest {

    @Test
    void onBuildingAddedAndOnCardTakenRewardOnlyNewCompletedSets() {
        // Input  : tribe already has 5 of 6 character types; onBuildingAdded gives initial reward,
        //          then onCardTaken(Inventor) (still 5 distinct in tribe) → no new reward,
        //          then add the Inventor and call onCardTaken again → completes the 6th set → reward.
        // Output : p.getFood()==5 (sum of initial + one-set completion rewards).
        Player p = TestUtils.player("p", 0, 0);
        p.getTribe().addCharacter(new HunterCard("h", Era.ERA_1, 0, false, 2));
        p.getTribe().addCharacter(new GathererCard("g", Era.ERA_1, 0, 2));
        p.getTribe().addCharacter(new BuilderCard("b", Era.ERA_1, 0, 1, 2));
        p.getTribe().addCharacter(new ShamanCard("s", Era.ERA_1, 0, 1, 2));
        p.getTribe().addCharacter(new ArtistCard("a", Era.ERA_1, 0, 2));

        FoodPerCompletedSetEffect effect = new FoodPerCompletedSetEffect();
        effect.onBuildingAdded(null, p);

        effect.onCardTaken(null, p, new InventorCard("i", Era.ERA_1, 0, InventionIcon.BOAT, 2));
        p.getTribe().addCharacter(new InventorCard("i", Era.ERA_1, 0, InventionIcon.BOAT, 2));
        effect.onCardTaken(null, p, null);

        assertEquals(5, p.getFood());
    }
}
