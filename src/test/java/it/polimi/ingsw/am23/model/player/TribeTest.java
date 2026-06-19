package it.polimi.ingsw.am23.model.player;

import it.polimi.ingsw.am23.model.cards.characters.ArtistCard;
import it.polimi.ingsw.am23.model.cards.characters.BuilderCard;
import it.polimi.ingsw.am23.model.cards.characters.GathererCard;
import it.polimi.ingsw.am23.model.cards.characters.HunterCard;
import it.polimi.ingsw.am23.model.cards.characters.InventorCard;
import it.polimi.ingsw.am23.model.cards.characters.ShamanCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.InventionIcon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TribeTest {

    @Test
    void addCharacterAndCountersWorkIncludingSetsAndPairs() {
        // Input  : add 5 of the 6 character types (no inventor) → 0 complete sets; then add 2 inventors and
        //          increment BOAT icon counter twice.
        // Output : count(HUNTER)==1, countCompletedSets()==1 (one of each type now), countInventorPairsByIcon()==1.
        Tribe tribe = new Tribe();

        tribe.addCharacter(new HunterCard("h1", Era.ERA_1, 0, false, 2));
        tribe.addCharacter(new GathererCard("g1", Era.ERA_1, 0, 2));
        tribe.addCharacter(new BuilderCard("b1", Era.ERA_1, 0, 1, 2));
        tribe.addCharacter(new ShamanCard("s1", Era.ERA_1, 0, 1, 2));
        tribe.addCharacter(new ArtistCard("a1", Era.ERA_1, 0, 2));

        assertEquals(0, tribe.countCompletedSets());

        tribe.addCharacter(new InventorCard("i1", Era.ERA_1, 0, InventionIcon.BOAT, 2));
        tribe.addCharacter(new InventorCard("i2", Era.ERA_1, 0, InventionIcon.BOAT, 2));
        tribe.incrementInventorIconCount(InventionIcon.BOAT);
        tribe.incrementInventorIconCount(InventionIcon.BOAT);

        assertEquals(1, tribe.count(CharacterType.HUNTER));
        assertEquals(1, tribe.countCompletedSets());
        assertEquals(1, tribe.countInventorPairsByIcon());
    }

    @Test
    void buildingsDiscountAndFlagsWork() {
        // Input  : add 2 builders (discounts 1 and 2); then add a building.
        // Output : getBuildingDiscount()==3 (sum of builder discounts); hasBuildings is false until a building is added.
        Tribe tribe = new Tribe();

        tribe.addCharacter(new BuilderCard("b1", Era.ERA_1, 0, 1, 2));
        tribe.addCharacter(new BuilderCard("b2", Era.ERA_1, 0, 2, 2));

        assertEquals(3, tribe.getBuildingDiscount());
        assertFalse(tribe.hasBuildings());

        tribe.addBuilding(new it.polimi.ingsw.am23.model.cards.BuildingCard("bd1", Era.ERA_1, 0, 0, new it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect(0)));
        assertTrue(tribe.hasBuildings());
    }

    @Test
    void shamanAndInventorSupportMethodsHandleEdgeCases() {
        // Input  : increment icon counters ARROW and BOAT; add 2 shaman stars; try negative addShamanStars and count(null).
        // Output : getDistinctInventionIcons()==2, totalShamanStars()==2;
        //          addShamanStars(-1) throws IllegalArgumentException; count(null) throws NullPointerException.
        Tribe tribe = new Tribe();

        tribe.incrementInventorIconCount(InventionIcon.ARROW);
        tribe.incrementInventorIconCount(InventionIcon.BOAT);
        assertEquals(2, tribe.getDistinctInventionIcons());

        tribe.addShamanStars(2);
        assertEquals(2, tribe.totalShamanStars());
        assertThrows(IllegalArgumentException.class, () -> tribe.addShamanStars(-1));
        assertThrows(NullPointerException.class, () -> tribe.count(null));
    }
}
