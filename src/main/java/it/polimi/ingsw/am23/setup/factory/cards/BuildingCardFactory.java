package it.polimi.ingsw.am23.setup.factory.cards;

import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.setup.definition.cards.BuildingCardDefinition;
import it.polimi.ingsw.am23.setup.factory.effects.BuildingEffectFactory;

import java.util.Map;
import java.util.Objects;

public class BuildingCardFactory {

    private final BuildingEffectFactory effectFactory;

    public BuildingCardFactory(BuildingEffectFactory effectFactory) {
        this.effectFactory = Objects.requireNonNull(effectFactory, "effectFactory cannot be null");
    }

    public BuildingCard create(BuildingCardDefinition definition) {
        Objects.requireNonNull(definition, "definition cannot be null");

        Map<String, Object> params = definition.getEffectParams() != null
                ? definition.getEffectParams()
                : Map.of();

        BuildingEffect effect = effectFactory.create(definition.getEffectType(), params);

        return new BuildingCard(
                definition.getId(),
                definition.getEra(),
                definition.getPoints(),
                definition.getFoodCost(),
                effect
        );
    }
}
