package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.player.Player;

/**
 * Building effect that grants 3 food every time the player completes a
 * new inventor pair after acquiring this building. The effect tracks the
 * last known pair count to award food only on net new completions.
 */
public class FoodPerInventorPairEffect implements BuildingEffect {

    private int lastKnownPairs = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBuildingAdded(Game game, Player player) {
        // Stores the pair count at purchase time to detect future increments
        lastKnownPairs = player.getTribe().countInventorPairsByIcon();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCardTaken(Game game, Player player, Card card) {
        int currentPairs = player.getTribe().countInventorPairsByIcon();
        if (currentPairs > lastKnownPairs) {
            player.applyFoodDelta(3 * (currentPairs - lastKnownPairs));
            lastKnownPairs = currentPairs;
        }
    }

}
