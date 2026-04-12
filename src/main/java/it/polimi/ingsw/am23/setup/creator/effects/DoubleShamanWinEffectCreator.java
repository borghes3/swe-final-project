package it.polimi.ingsw.am23.setup.creator.effects;

import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.buildings.DoubleShamanWinEffect;

import java.util.Map;

public class DoubleShamanWinEffectCreator extends AbstractBuildingEffectCreator {

    @Override
    public String supportedType() {
        return "DOUBLE_SHAMAN_WIN";
    }

    @Override
    public BuildingEffect create(Map<String, Object> effectParams) {
        return new DoubleShamanWinEffect();
    }
}
