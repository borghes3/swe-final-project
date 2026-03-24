package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.player.Player;

public class FlatEndGamePointsEffect implements BuildingEffect{
    private final int points;

    public FlatEndGamePointsEffect(int points) {
        this.points = points;
    }

    @Override
    public int getEndGamePoints(Game game, Player player){
        return points;
    }
}
