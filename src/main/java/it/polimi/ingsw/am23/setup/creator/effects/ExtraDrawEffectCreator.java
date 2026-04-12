package it.polimi.ingsw.am23.setup.creator.effects;

import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.buildings.ExtraDrawEffect;

import java.util.Map;

public class ExtraDrawEffectCreator extends AbstractBuildingEffectCreator {

    @Override
    public String supportedType() {
        return "EXTRA_DRAW";
    }

    @Override
    public BuildingEffect create(Map<String, Object> effectParams) {
        return new ExtraDrawEffect();
    }
}
