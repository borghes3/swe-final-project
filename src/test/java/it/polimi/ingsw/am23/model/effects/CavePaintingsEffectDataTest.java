package it.polimi.ingsw.am23.model.effects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CavePaintingsEffectDataTest {

    @Test
    void addExtraFoodAccumulatesValue() {
        CavePaintingsEffectData data = new CavePaintingsEffectData();
        data.addExtraFood(2);
        data.addExtraFood(3);
        assertEquals(5, data.getExtraFood());
    }
}
