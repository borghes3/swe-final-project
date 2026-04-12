package it.polimi.ingsw.am23.setup.factory.effects;

import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.setup.creator.effects.BuildingEffectCreator;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BuildingEffectFactory {

    private final Map<String, BuildingEffectCreator> creators;

    public BuildingEffectFactory(List<BuildingEffectCreator> creators) {
        Objects.requireNonNull(creators, "creators cannot be null");

        this.creators = creators.stream()
                .collect(Collectors.toUnmodifiableMap( //toUnmodifiableMap per gestire eccezioni dovute da creator con stesso type
                        BuildingEffectCreator::supportedType,
                        Function.identity()
                ));
    }

    public BuildingEffect create(String effectType, Map<String, Object> effectParams) {
        Objects.requireNonNull(effectType, "effectType cannot be null");

        BuildingEffectCreator creator = creators.get(effectType);
        if (creator == null) {
            throw new IllegalArgumentException("Unsupported building effect type: " + effectType);
        }

        Map<String, Object> params = effectParams != null ? effectParams : Map.of(); //Map.of() visto che i parametri possono essere null
        return creator.create(params);
    }
}