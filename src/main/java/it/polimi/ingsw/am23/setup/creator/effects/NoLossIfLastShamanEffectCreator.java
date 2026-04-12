package it.polimi.ingsw.am23.setup.creator.effects;

import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.buildings.NoLossIfLastShamanEffect;

import java.util.Map;

public class NoLossIfLastShamanEffectCreator extends AbstractBuildingEffectCreator {

    @Override
    public String supportedType() {
        return "NO_LOSS_IF_LAST_SHAMAN";
    }

    @Override
    public BuildingEffect create(Map<String, Object> effectParams) {
        return new NoLossIfLastShamanEffect();
    }
}
