package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.player.Player;

public class FoodPerInventorPairEffect implements BuildingEffect {

    private int lastKnownPairs = 0;

    @Override
    public void onBuildingAdded(Player player) {
        // memorizzo le coppie già esistenti
        lastKnownPairs = player.getTribe().countInventorPairsByIcon();
    }

    @Override
    public void onCardTaken(Game game, Player player, Card card) {
        int currentPairs = player.getTribe().countInventorPairsByIcon();
        if (currentPairs > lastKnownPairs) {
            player.applyFoodDelta(3 * (currentPairs - lastKnownPairs));
            lastKnownPairs = currentPairs;
        }
    }

}
