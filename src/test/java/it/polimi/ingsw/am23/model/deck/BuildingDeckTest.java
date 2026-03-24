package it.polimi.ingsw.am23.model.deck;

import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.enums.Era;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BuildingDeckTest {

    @Test
    void drawAndPeekOperateOnEraDeck() {
        Map<Era, List<BuildingCard>> cardsByEra = new EnumMap<>(Era.class);
        cardsByEra.put(Era.ERA_1, List.of(
                new BuildingCard("b1", Era.ERA_1, 0, 0, new BuildingEffect() {
                }),
                new BuildingCard("b2", Era.ERA_1, 0, 0, new BuildingEffect() {
                })
        ));
        BuildingDeck deck = new BuildingDeck(cardsByEra);

        assertEquals(2, deck.size(Era.ERA_1));
        assertEquals("b1", deck.peekTop(Era.ERA_1).getId());
        assertEquals("b1", deck.draw(Era.ERA_1).getId());
        assertEquals(1, deck.size(Era.ERA_1));
    }

    @Test
    void drawThrowsWhenEraDeckEmpty() {
        BuildingDeck deck = new BuildingDeck(Map.of());
        assertThrows(IllegalArgumentException.class, () -> deck.draw(Era.ERA_1));
        assertThrows(IllegalArgumentException.class, () -> deck.peekTop(Era.ERA_1));
    }

}
