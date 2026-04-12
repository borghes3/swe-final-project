package it.polimi.ingsw.am23.setup.creator.effects;

import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.buildings.HuntingRewardPerHunterEffect;

import java.util.Map;

public class HuntingRewardPerHunterEffectCreator extends AbstractBuildingEffectCreator {

    @Override
    public String supportedType() {
        return "HUNTING_REWARD_PER_HUNTER";
    }

    @Override
    public BuildingEffect create(Map<String, Object> effectParams) {
        return new HuntingRewardPerHunterEffect();
    }
}
