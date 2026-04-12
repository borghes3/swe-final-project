package it.polimi.ingsw.am23.setup.creator.effects;

import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.buildings.FoodPerInventorPairEffect;

import java.util.Map;

public class FoodPerInventorPairEffectCreator extends AbstractBuildingEffectCreator {

    @Override
    public String supportedType() {
        return "FOOD_PER_INVENTOR_PAIR";
    }

    @Override
    public BuildingEffect create(Map<String, Object> effectParams) {
        return new FoodPerInventorPairEffect();
    }
}
