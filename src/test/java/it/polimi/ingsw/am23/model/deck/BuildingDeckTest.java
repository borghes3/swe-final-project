package it.polimi.ingsw.am23.model.deck;

import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.enums.Era;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BuildingDeckTest {

    @Test
    void drawPeekAndSizeBehavePerEra() {
        Map<Era, List<it.polimi.ingsw.am23.model.cards.BuildingCard>> map = new EnumMap<>(Era.class);
        map.put(Era.ERA_1, List.of(
                TestUtils.building("b1", Era.ERA_1, 0, 0, new it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect(0)),
                TestUtils.building("b2", Era.ERA_1, 0, 0, new it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect(0))
        ));
        BuildingDeck deck = new BuildingDeck(map);

        assertEquals("b1", deck.peekTop(Era.ERA_1).getId());
        assertEquals("b1", deck.draw(Era.ERA_1).getId());
        assertEquals(1, deck.size(Era.ERA_1));
        assertEquals(1, deck.getCardsForEra(Era.ERA_1).size());

        deck.draw(Era.ERA_1);
        assertThrows(IllegalArgumentException.class, () -> deck.draw(Era.ERA_1));
        assertFalse(deck.isEmpty(Era.ERA_1));
    }
}
