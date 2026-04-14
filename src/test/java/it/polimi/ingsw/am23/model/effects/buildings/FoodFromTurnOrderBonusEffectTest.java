package it.polimi.ingsw.am23.model.effects.buildings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FoodFromTurnOrderBonusEffectTest {

    @Test
    void modifyTurnOrderFoodAddsConfiguredBonus() {
        assertEquals(5, new FoodFromTurnOrderBonusEffect(2).modifyTurnOrderFood(null, null, 3));
    }
}
