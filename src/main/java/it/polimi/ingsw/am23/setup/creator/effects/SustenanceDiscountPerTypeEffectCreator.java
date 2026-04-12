package it.polimi.ingsw.am23.setup.creator.effects;

import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.buildings.SustenanceDiscountPerTypeEffect;

import java.util.Map;

public class SustenanceDiscountPerTypeEffectCreator extends AbstractBuildingEffectCreator {

    @Override
    public String supportedType() {
        return "SUSTENANCE_DISCOUNT_PER_TYPE";
    }

    @Override
    public BuildingEffect create(Map<String, Object> effectParams) {
        return new SustenanceDiscountPerTypeEffect(
                readCharacterType(effectParams, "targetType"),
                1
        );
    }
}
