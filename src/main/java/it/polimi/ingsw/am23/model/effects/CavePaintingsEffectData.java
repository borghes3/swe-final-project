package it.polimi.ingsw.am23.model.effects;

/**
 * Mutable accumulator collecting the bonuses contributed by building effects
 * during the resolution of a Cave Paintings event.
 */
public class CavePaintingsEffectData {

    private int extraFood;

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
}
