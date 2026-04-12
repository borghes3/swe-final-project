package it.polimi.ingsw.am23.setup.creator.effects;

import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.buildings.FoodPerCompletedSetEffect;

import java.util.Map;

public class FoodPerCompletedSetEffectCreator extends AbstractBuildingEffectCreator {

    @Override
    public String supportedType() {
        return "FOOD_PER_COMPLETED_SET";
    }

    @Override
    public BuildingEffect create(Map<String, Object> effectParams) {
        return new FoodPerCompletedSetEffect();
    }
}
