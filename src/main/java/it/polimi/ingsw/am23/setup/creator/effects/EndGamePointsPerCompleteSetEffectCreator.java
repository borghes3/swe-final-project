package it.polimi.ingsw.am23.setup.creator.effects;

import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.buildings.EndGamePointsPerCompleteSetEffect;

import java.util.Map;

public class EndGamePointsPerCompleteSetEffectCreator extends AbstractBuildingEffectCreator {

    @Override
    public String supportedType() {
        return "END_GAME_POINTS_PER_COMPLETE_SET";
    }

    @Override
    public BuildingEffect create(Map<String, Object> effectParams) {
        return new EndGamePointsPerCompleteSetEffect(6);
    }
}
