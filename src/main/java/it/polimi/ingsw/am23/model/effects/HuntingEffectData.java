package it.polimi.ingsw.am23.model.effects;

/**
 * Mutable accumulator collecting the bonuses contributed by building
 * effects during the resolution of a Hunting event.
 */
public class HuntingEffectData {

    private int extraFood;
    private int extraPoints;

    /**
     * @return the extra food accumulated so far
     */
    public int getExtraFood() {
        return extraFood;
    }

    /**
     * Adds extra food to the accumulator.
     *
     * @param extraFood food to add
     */
    public void addExtraFood(int extraFood) {
        this.extraFood += extraFood;
    }

    /**
     * @return the extra prestige points accumulated so far
     */
    public int getExtraPoints() {
        return extraPoints;
    }

    /**
     * Adds extra prestige points to the accumulator.
     *
     * @param extraPoints prestige points to add
     */
    public void addExtraPoints(int extraPoints) {
        this.extraPoints += extraPoints;
    }
}
