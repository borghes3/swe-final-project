package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.player.Player;

/**
 * Building effect that grants 5 food every time the player completes a new
 * character set after acquiring this building. The effect tracks the last
 * known set count to award food only on net new completions.
 */
public class FoodPerCompletedSetEffect implements BuildingEffect {

    private int lastKnownSetCount = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBuildingAdded(Game game, Player player) {
        // Stores the set count at purchase time to detect future increments
        lastKnownSetCount = player.getTribe().countCompletedSets();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCardTaken(Game game, Player player, Card card) {
        int currentSets = player.getTribe().countCompletedSets();
        if (currentSets > lastKnownSetCount) {
            player.applyFoodDelta(5 * (currentSets - lastKnownSetCount));
            lastKnownSetCount = currentSets;
        }
    }
}
