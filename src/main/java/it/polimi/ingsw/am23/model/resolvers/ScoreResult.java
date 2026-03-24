package it.polimi.ingsw.am23.model.resolvers;

import it.polimi.ingsw.am23.model.player.Player;

public class ScoreResult {
    public Player player;
    public int foodPoints;
    public int PP;

    public ScoreResult(Player player, int foodPoints, int PP) {
        this.player = player;
        this.foodPoints = foodPoints;
        this.PP = PP;
    }
}
