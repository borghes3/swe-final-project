package it.polimi.ingsw.am23.setup.creator.effects;

import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.buildings.CavePaintingsFoodPerArtistEffect;

import java.util.Map;

public class CavePaintingsFoodPerArtistEffectCreator extends AbstractBuildingEffectCreator {

    @Override
    public String supportedType() {
        return "CAVE_PAINTINGS_FOOD_PER_ARTIST";
    }

    @Override
    public BuildingEffect create(Map<String, Object> effectParams) {
        return new CavePaintingsFoodPerArtistEffect(1);
    }
}
