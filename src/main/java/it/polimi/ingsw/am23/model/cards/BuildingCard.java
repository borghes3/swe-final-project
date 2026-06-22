package it.polimi.ingsw.am23.model.cards;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.state.BuildingCardState;
import it.polimi.ingsw.am23.model.state.CardState;

import java.util.Objects;

/**
 * Concrete card representing a building. Each building is bought at a food
 * cost and grants a persistent {@link BuildingEffect} once owned.
 */
public class BuildingCard extends Card {
    private final int foodCost;
    private final BuildingEffect effect;

    /**
     * Builds a new building card.
     *
     * @param id       unique identifier of the card
     * @param era      era the card belongs to
     * @param points   printed victory points
     * @param foodCost food cost required to purchase the card
     * @param effect   persistent effect granted upon purchase
     */
    public BuildingCard(String id, Era era, int points, int foodCost, BuildingEffect effect) {
        super(id, era, points);
        this.foodCost = foodCost;
        this.effect = Objects.requireNonNull(effect, "effect cannot be null");
    }

    /**
     * @return the food cost required to purchase the card
     */
    public int getFoodCost() {
        return foodCost;
    }

    /**
     * @return the persistent effect granted by the building
     */
    public BuildingEffect getEffect() {
        return effect;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canBeTaken() {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>Adds the building to the buyer's tribe.</p>
     */
    @Override
    public void onTaken(Game game, Player player) {
        Objects.requireNonNull(game, "game cannot be null");
        Objects.requireNonNull(player, "player cannot be null");

        player.getTribe().addBuilding(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardState toState() {
        return new BuildingCardState(
                getId(),
                getEra(),
                getPoints(),
                foodCost,
                effect.getClass().getSimpleName()
        );
    }
}
