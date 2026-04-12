package it.polimi.ingsw.am23.setup.creator.effects;

import it.polimi.ingsw.am23.model.enums.CharacterType;

import java.util.Map;

public abstract class AbstractBuildingEffectCreator implements BuildingEffectCreator {

    protected CharacterType readCharacterType(Map<String, Object> params, String key) {
        Object value = params.get(key);

        if (value == null) {
            throw new IllegalArgumentException("Missing effect parameter: " + key);
        }

        if (!(value instanceof String stringValue)) {
            throw new IllegalArgumentException("Effect parameter '" + key + "' must be a string");
        }

        try {
            return CharacterType.valueOf(stringValue);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid CharacterType for parameter '" + key + "': " + stringValue,
                    e
            );
        }
    }

    protected int endGamePointsForType(CharacterType characterType) {
        return switch (characterType) {
            case BUILDER -> 4;
            case SHAMAN -> 4;
            case INVENTOR -> 2;
            case HUNTER -> 3;
            case ARTIST -> 4;
            case GATHERER -> 4;
        };
    }
}
