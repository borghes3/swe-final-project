package it.polimi.ingsw.am23.setup.creator.effects;

import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.buildings.EndGamePointsPerCharacterTypeEffect;
import it.polimi.ingsw.am23.model.enums.CharacterType;

import java.util.Map;

public class EndGamePointsPerCharacterTypeEffectCreator extends AbstractBuildingEffectCreator {

    @Override
    public String supportedType() {
        return "END_GAME_POINTS_PER_CHARACTER_TYPE";
    }

    @Override
    public BuildingEffect create(Map<String, Object> effectParams) {
        CharacterType targetType = readCharacterType(effectParams, "targetType");
        return new EndGamePointsPerCharacterTypeEffect(
                targetType,
                endGamePointsForType(targetType)
        );
    }
}
