package it.polimi.ingsw.am23.model.resolvers;

import it.polimi.ingsw.am23.model.player.Player;

/**
 * Mutable in-memory per-player score record produced by the {@link
 * ScoreCalculator}. Carries the original {@link Player} reference along
 * with the food-derived points and the cumulative prestige points.
 */
public class ScoreResult {
    /**
     * Player the score refers to.
     */
    public Player player;
    /**
     * Points coming from the leftover food reserve.
     */
    public int foodPoints;
    /**
     * Total prestige points.
     */
    public int PP;

    /**
     * Builds a new score result.
     *
     * @param player     player the score refers to
     * @param foodPoints food-derived points
     * @param PP         cumulative prestige points
     */
    public ScoreResult(Player player, int foodPoints, int PP) {
        this.player = player;
        this.foodPoints = foodPoints;
        this.PP = PP;
    }
}
