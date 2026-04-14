package it.polimi.ingsw.am23.model.effects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HuntingEffectDataTest {

    @Test
    void addMethodsAccumulateFoodAndPoints() {
        HuntingEffectData data = new HuntingEffectData();
        data.addExtraFood(2);
        data.addExtraPoints(4);
        data.addExtraFood(1);
        data.addExtraPoints(1);

        assertEquals(3, data.getExtraFood());
        assertEquals(5, data.getExtraPoints());
    }
}
