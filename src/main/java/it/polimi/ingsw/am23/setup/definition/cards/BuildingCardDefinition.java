package it.polimi.ingsw.am23.setup.definition.cards;

import java.util.Map;

public final class BuildingCardDefinition extends CardDefinition {
    private int foodCost;
    private String effectType;
    private Map<String, Object> effectParams;

    public int getFoodCost() { return foodCost; }
    public String getEffectType() { return effectType; }
    public Map<String, Object> getEffectParams() { return effectParams; }
}
