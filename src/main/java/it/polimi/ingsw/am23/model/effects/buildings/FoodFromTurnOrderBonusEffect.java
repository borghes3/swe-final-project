package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.player.Player;

public class FoodFromTurnOrderBonusEffect implements BuildingEffect {

    private final int foodBonus;

    public FoodFromTurnOrderBonusEffect(int foodBonus) {
        this.foodBonus = foodBonus;
    }

    @Override
    public int modifyTurnOrderFood(Game game, Player player, int currentFood){
        return currentFood + foodBonus;
    }

}
