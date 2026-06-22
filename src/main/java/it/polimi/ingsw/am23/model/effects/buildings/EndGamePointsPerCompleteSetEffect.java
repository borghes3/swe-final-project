package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.player.Player;

/**
 * End-of-game building effect that awards prestige points per complete
 * character set in the player's tribe.
 */
public class EndGamePointsPerCompleteSetEffect implements BuildingEffect{

    private int pointsPerSet;

    /**
     * Builds a new effect.
     *
     * @param pointsPerSet prestige points awarded per completed character set
     */
    public EndGamePointsPerCompleteSetEffect(int pointsPerSet){
        this.pointsPerSet = pointsPerSet;
    }

    /** {@inheritDoc} */
    @Override
    public int getEndGamePoints(Game game, Player player){
        return player.getTribe().countCompletedSets()* pointsPerSet;
    }
}
