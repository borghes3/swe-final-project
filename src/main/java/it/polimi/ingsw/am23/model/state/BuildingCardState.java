package it.polimi.ingsw.am23.model.state;

import it.polimi.ingsw.am23.model.enums.CardKind;
import it.polimi.ingsw.am23.model.enums.Era;

public final class BuildingCardState extends CardState {

    private final int foodCost;
    private final String effectId; //TODO: fare enum effetti così da non usare sta brutta roba

    public BuildingCardState(String cardId, Era era, int printedPoints, int foodCost, String effectId) {
        super(cardId, era, printedPoints);
        this.foodCost = foodCost;
        this.effectId = effectId;
    }
    public int getFoodCost() {
        return foodCost;
    }

    public String getEffectId() {
        return effectId;
    }

    @Override
    public CardKind getCardKind() {
        return CardKind.BUILDING;
    }
}
