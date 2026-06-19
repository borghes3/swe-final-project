package it.polimi.ingsw.am23.model.effects.buildings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FoodFromTurnOrderBonusEffectTest {

    @Test
    void modifyTurnOrderFoodAddsConfiguredBonus() {
        // Input  : FoodFromTurnOrderBonusEffect(bonus=2); call modifyTurnOrderFood(null, null, currentFood=3).
        // Output : 5 (3 + 2 bonus).
        assertEquals(5, new FoodFromTurnOrderBonusEffect(2).modifyTurnOrderFood(null, null, 3));
    }
}
