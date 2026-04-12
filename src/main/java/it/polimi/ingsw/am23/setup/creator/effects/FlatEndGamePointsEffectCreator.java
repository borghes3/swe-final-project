package it.polimi.ingsw.am23.setup.creator.effects;

import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect;

import java.util.Map;

public class FlatEndGamePointsEffectCreator extends AbstractBuildingEffectCreator {

    @Override
    public String supportedType() {
        return "FLAT_END_GAME_POINTS";
    }

    @Override
    public BuildingEffect create(Map<String, Object> effectParams) {
        return new FlatEndGamePointsEffect(25);
    }
}
