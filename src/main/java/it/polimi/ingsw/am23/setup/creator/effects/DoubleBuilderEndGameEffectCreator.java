package it.polimi.ingsw.am23.setup.creator.effects;

import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.buildings.DoubleBuilderEndGameEffect;

import java.util.Map;

public class DoubleBuilderEndGameEffectCreator extends AbstractBuildingEffectCreator {

    @Override
    public String supportedType() {
        return "DOUBLE_BUILDER_END_GAME";
    }

    @Override
    public BuildingEffect create(Map<String, Object> effectParams) {
        return new DoubleBuilderEndGameEffect();
    }
}
