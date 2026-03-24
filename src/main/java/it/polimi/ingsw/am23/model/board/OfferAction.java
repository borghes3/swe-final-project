package it.polimi.ingsw.am23.model.board;

public class OfferAction {

    private final int topDrawCount;
    private final int bottomDrawCount;
    private final int foodReward;

    public OfferAction(int upperRowCount, int lowerRowCount, int foodReward) {
        this.topDrawCount = upperRowCount;
        this.bottomDrawCount = lowerRowCount;
        this.foodReward = foodReward;
    }

    public int getUpperDrawRowCount() {
        return topDrawCount;
    }
    public int getBottomDrawCount() {
        return bottomDrawCount;
    }
    public int getFoodReward() {
        return foodReward;
    }
}
//chiedere se si possono usare i record (introdotti in java 16) che sono una classe progettata per modellare dati immutabili
//utili per DTO