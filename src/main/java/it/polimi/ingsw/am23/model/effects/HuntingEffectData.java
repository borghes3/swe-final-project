package it.polimi.ingsw.am23.model.effects;

public class HuntingEffectData {

    private int extraFood;
    private int extraPoints;

    public int getExtraFood() {
        return extraFood;
    }

    public void addExtraFood(int extraFood) {
        this.extraFood += extraFood;
    }

    public int getExtraPoints() {
        return extraPoints;
    }

    public void addExtraPoints(int extraPoints) {
        this.extraPoints += extraPoints;
    }
}
