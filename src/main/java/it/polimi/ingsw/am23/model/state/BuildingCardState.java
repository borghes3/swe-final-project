package it.polimi.ingsw.am23.model.state;

import it.polimi.ingsw.am23.model.enums.CardKind;
import it.polimi.ingsw.am23.model.enums.Era;

/**
 * Immutable snapshot of a building card.
 */
public final class BuildingCardState extends CardState {

    private final int foodCost;
    private final String effectId;

    /**
     * Builds a new building card snapshot.
     *
     * @param cardId        unique identifier of the card
     * @param era           era the card belongs to
     * @param printedPoints victory points printed on the card
     * @param foodCost      food cost required to purchase the card
     * @param effectId      identifier of the effect attached to the card
     */
    public BuildingCardState(String cardId, Era era, int printedPoints, int foodCost, String effectId) {
        super(cardId, era, printedPoints);
        this.foodCost = foodCost;
        this.effectId = effectId;
    }

    /**
     * @return the food cost required to purchase the building
     */
    public int getFoodCost() {
        return foodCost;
    }

    /**
     * @return the identifier of the effect attached to the building
     */
    public String getEffectId() {
        return effectId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardKind getCardKind() {
        return CardKind.BUILDING;
    }
}
