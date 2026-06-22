package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.player.Player;

/**
 * Building effect that schedules an extra draw turn for the owning player
 * once the building is purchased.
 */
public class ExtraDrawEffect implements BuildingEffect {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBuildingAdded(Game game, Player player) {
        game.setPendingExtraDrawPlayerId(player.getId());
    }
}
