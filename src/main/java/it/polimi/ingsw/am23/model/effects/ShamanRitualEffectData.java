package it.polimi.ingsw.am23.model.effects;

/**
 * Mutable accumulator collecting the bonuses contributed by building
 * effects during the resolution of a Shaman Ritual event. Tracks bonus
 * stars and the loss/win modifiers.
 */
public class ShamanRitualEffectData {

    private int bonusStars;
    private boolean ignoreLoss;
    private boolean doubleWin;

    /** @return the bonus stars accumulated so far */
    public int getBonusStars() {
        return bonusStars;
    }

    /**
     * Sets the bonus stars contributed by the effects.
     *
     * @param bonusStars bonus stars to set
     */
    public void setBonusStars(int bonusStars) {
        this.bonusStars = bonusStars;
    }

    /** @return {@code true} if the player should not lose points when last */
    public boolean ignoreLoss() {
        return ignoreLoss;
    }

    /**
     * Configures whether the loss modifier should be suppressed.
     *
     * @param ignoreLoss flag value to set
     */
    public void setIgnoreLoss(boolean ignoreLoss) {
        this.ignoreLoss = ignoreLoss;
    }

    /** @return {@code true} if the player should score double when winning */
    public boolean doubleWin() {
        return doubleWin;
    }

    /**
     * Configures whether the win modifier should be doubled.
     *
     * @param doubleWin flag value to set
     */
    public void setDoubleWin(boolean doubleWin) {
        this.doubleWin = doubleWin;
    }
}
