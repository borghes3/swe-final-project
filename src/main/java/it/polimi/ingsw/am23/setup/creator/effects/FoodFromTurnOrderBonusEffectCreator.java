package it.polimi.ingsw.am23.setup.creator.effects;

import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.buildings.FoodFromTurnOrderBonusEffect;

import java.util.Map;

public class FoodFromTurnOrderBonusEffectCreator extends AbstractBuildingEffectCreator {

    @Override
    public String supportedType() {
        return "FOOD_FROM_TURN_ORDER_BONUS";
    }

    @Override
    public BuildingEffect create(Map<String, Object> effectParams) {
        return new FoodFromTurnOrderBonusEffect(1);
    }
}
