package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.player.Player;

/**
 * End-of-game building effect that awards a fixed amount of prestige
 * points regardless of the player's tribe composition.
 */
public class FlatEndGamePointsEffect implements BuildingEffect {
    private final int points;

    /**
     * Builds a new effect.
     *
     * @param points fixed prestige points awarded at end of game
     */
    public FlatEndGamePointsEffect(int points) {
        this.points = points;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEndGamePoints(Game game, Player player) {
        return points;
    }
}
