package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.player.Player;

public class EndGamePointsPerCompleteSetEffect implements BuildingEffect{

    private int pointsPerSet;

    public EndGamePointsPerCompleteSetEffect(int pointsPerSet){
        this.pointsPerSet = pointsPerSet;
    }

    @Override
    public int getEndGamePoints(Game game, Player player){
        return player.getTribe().countCompletedSets()* pointsPerSet;
    }
}
