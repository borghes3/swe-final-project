package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.player.Player;

public class FoodPerCompletedSetEffect implements BuildingEffect {

    //TODO: rifare questa parte dei set
    private int lastKnownSetCount = 0;

    @Override
    public void onBuildingAdded(Player player){
        // memorizzo numero attuale di set
        lastKnownSetCount = player.getTribe().countCompletedSets();
    }

    @Override
    public void onCardTaken(Game game, Player player, Card card) {
        int currentSets = player.getTribe().countCompletedSets();
        if (currentSets > lastKnownSetCount) {
            player.addFood(5 * (currentSets - lastKnownSetCount));
            lastKnownSetCount = currentSets;
        }
    }
}