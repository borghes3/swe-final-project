package it.polimi.ingsw.am23.model.effects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HuntingEffectDataTest {

    @Test
    void addMethodsAccumulateFoodAndPoints() {
        // Input  : addExtraFood(2), addExtraPoints(4), addExtraFood(1), addExtraPoints(1).
        // Output : getExtraFood()==3 and getExtraPoints()==5.
        HuntingEffectData data = new HuntingEffectData();
        data.addExtraFood(2);
        data.addExtraPoints(4);
        data.addExtraFood(1);
        data.addExtraPoints(1);

        assertEquals(3, data.getExtraFood());
        assertEquals(5, data.getExtraPoints());
    }
}
