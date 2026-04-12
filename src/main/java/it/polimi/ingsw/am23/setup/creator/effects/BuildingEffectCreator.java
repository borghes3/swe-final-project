package it.polimi.ingsw.am23.setup.creator.effects;

import it.polimi.ingsw.am23.model.effects.BuildingEffect;

import java.util.Map;

public interface BuildingEffectCreator {
    String supportedType();
    BuildingEffect create(Map<String, Object> effectParams);
}
