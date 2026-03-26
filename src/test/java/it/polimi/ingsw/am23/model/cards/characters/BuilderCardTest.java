package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.enums.Era;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuilderCardTest {

    @Test
    void getDiscountReturnsValue() {
        BuilderCard card = new BuilderCard("b1", Era.ERA_1, 0, 2, 1);
        assertEquals(2, card.getDiscount());
    }
}
