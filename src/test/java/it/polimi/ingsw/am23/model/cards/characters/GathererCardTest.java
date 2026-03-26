package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GathererCardTest {

    @Test
    void characterTypeIsGatherer() {
        GathererCard card = new GathererCard("g1", Era.ERA_1, 0, 3);
        assertEquals(CharacterType.GATHERER, card.getCharacterType());
    }
}
