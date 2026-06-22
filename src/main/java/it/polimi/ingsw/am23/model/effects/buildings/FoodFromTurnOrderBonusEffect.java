package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.player.Player;

/**
 * Building effect that adds a flat food bonus on top of the food granted
 * by the turn order slot the owning player landed on.
 */
public class FoodFromTurnOrderBonusEffect implements BuildingEffect {

    private final int foodBonus;

    /**
     * Builds a new effect.
     *
     * @param foodBonus extra food added to the turn order delta
     */
    public FoodFromTurnOrderBonusEffect(int foodBonus) {
        this.foodBonus = foodBonus;
    }

    /** {@inheritDoc} */
    @Override
    public int modifyTurnOrderFood(Game game, Player player, int currentFood){
        return currentFood + foodBonus;
    }

}
