package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.enums.Era;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShamanCardTest {

    @Test
    void getStarsReturnsValue() {
        ShamanCard card = new ShamanCard("s1", Era.ERA_1, 0, 3);
        assertEquals(3, card.getStars());
    }
}
