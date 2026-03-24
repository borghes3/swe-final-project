package it.polimi.ingsw.am23.model.effects;

public class ShamanRitualEffectData {

    private int bonusStars;
    private boolean ignoreLoss;
    private boolean doubleWin;

    public int getBonusStars() {
        return bonusStars;
    }
    public void setBonusStars(int bonusStars) {
        this.bonusStars = bonusStars;
    }
    public boolean ignoreLoss() {
        return ignoreLoss;
    }
    public void setIgnoreLoss(boolean ignoreLoss) {
        this.ignoreLoss = ignoreLoss;
    }
    public boolean doubleWin() {
        return doubleWin;
    }
    public void setDoubleWin(boolean doubleWin) {
        this.doubleWin = doubleWin;
    }
}
