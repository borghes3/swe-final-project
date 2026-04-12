package it.polimi.ingsw.am23.setup.creator.effects;

import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.buildings.ShamanBonusStarsEffect;

import java.util.Map;

public class ShamanBonusStarsEffectCreator extends AbstractBuildingEffectCreator {

    @Override
    public String supportedType() {
        return "SHAMAN_BONUS_STARS";
    }

    @Override
    public BuildingEffect create(Map<String, Object> effectParams) {
        return new ShamanBonusStarsEffect(3);
    }
}
