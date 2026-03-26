package it.polimi.ingsw.am23.model.player;

import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.characters.ArtistCard;
import it.polimi.ingsw.am23.model.cards.characters.BuilderCard;
import it.polimi.ingsw.am23.model.cards.characters.GathererCard;
import it.polimi.ingsw.am23.model.cards.characters.HunterCard;
import it.polimi.ingsw.am23.model.cards.characters.InventorCard;
import it.polimi.ingsw.am23.model.cards.characters.ShamanCard;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.InventionIcon;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TribeTest {

    @Test
    void addCharacterCountsByType() {
        Tribe tribe = new Tribe();
        tribe.addCharacter(new HunterCard("h1", Era.ERA_1, 0, false, 3));
        tribe.addCharacter(new HunterCard("h2", Era.ERA_1, 0, false, 3));
        tribe.addCharacter(new GathererCard("g1", Era.ERA_1, 0, 3));

        assertEquals(2, tribe.count(CharacterType.HUNTER));
        assertEquals(1, tribe.count(CharacterType.GATHERER));
        assertEquals(0, tribe.count(CharacterType.BUILDER));
    }

    @Test
    void totalShamanStarsSumsOnlyShamans() {
        Tribe tribe = new Tribe();
        tribe.addCharacter(new ShamanCard("s1", Era.ERA_1, 0, 2, 3));
        tribe.addCharacter(new ShamanCard("s2", Era.ERA_1, 0, 3, 3));
        tribe.addCharacter(new HunterCard("h1", Era.ERA_1, 0, false, 4));

        assertEquals(5, tribe.totalShamanStars());
    }

    @Test
    void countCompletedSetsUsesMinimumAcrossTypes() {
        Tribe tribe = new Tribe();
        tribe.addCharacter(new HunterCard("h1", Era.ERA_1, 0, false, 3));
        tribe.addCharacter(new GathererCard("g1", Era.ERA_1, 0, 3));
        tribe.addCharacter(new BuilderCard("b1", Era.ERA_1, 0, 0, 2));
        tribe.addCharacter(new ShamanCard("s1", Era.ERA_1, 0, 1, 3));
        tribe.addCharacter(new ArtistCard("a1", Era.ERA_1, 0, 2));
        tribe.addCharacter(new InventorCard("i1", Era.ERA_1, 0, InventionIcon.BOAT, 3));

        assertEquals(1, tribe.countCompletedSets());
    }

    @Test
    void countInventorPairsByIconCountsPairs() {
        Tribe tribe = new Tribe();
        tribe.addCharacter(new InventorCard("i1", Era.ERA_1, 0, InventionIcon.BOAT, 4));
        tribe.addCharacter(new InventorCard("i2", Era.ERA_1, 0, InventionIcon.BOAT, 3));
        tribe.addCharacter(new InventorCard("i3", Era.ERA_1, 0, InventionIcon.BOAT, 2));
        tribe.addCharacter(new InventorCard("i4", Era.ERA_1, 0, InventionIcon.ARROW, 2));

        assertEquals(1, tribe.countInventorPairsByIcon());
    }

    @Test
    void distinctInventionIconsCountsUniqueIcons() {
        Tribe tribe = new Tribe();
        tribe.addCharacter(new InventorCard("i1", Era.ERA_1, 0, InventionIcon.BOAT, 2));
        tribe.addCharacter(new InventorCard("i2", Era.ERA_1, 0, InventionIcon.BOAT, 2));
        tribe.addCharacter(new InventorCard("i3", Era.ERA_1, 0, InventionIcon.ARROW,2));

        assertEquals(2, tribe.getDistinctInventionIcons());
    }

    @Test
    void hasBuildingsReflectsState() {
        Tribe tribe = new Tribe();
        assertFalse(tribe.hasBuildings());
        tribe.addBuilding(new BuildingCard("b1", Era.ERA_1, 0, 0, new BuildingEffect() {
        }));
        assertTrue(tribe.hasBuildings());
    }

    @Test
    void getCharactersReturnsUnmodifiableList() {
        Tribe tribe = new Tribe();
        assertThrows(UnsupportedOperationException.class, () -> tribe.getCharacters().add(new ArtistCard("a1", Era.ERA_1, 0, 2)));
    }

    @Test
    void addCharacterRejectsNull() {
        Tribe tribe = new Tribe();
        assertThrows(NullPointerException.class, () -> tribe.addCharacter(null));
    }

    @Test
    void addBuildingRejectsNull() {
        Tribe tribe = new Tribe();
        assertThrows(NullPointerException.class, () -> tribe.addBuilding(null));
    }
}
