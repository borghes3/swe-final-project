package it.polimi.ingsw.am23.model.cards.turnorder;

public class TurnOrderSlot {

    private final int index;
    private final int foodDelta;        //il delta di punti è sempre multiplo di foodDelta in teoria
    private String playerIdInSlot;

    public TurnOrderSlot(int index, int foodDelta, String playerIdInSlot) {
        this.index = index;
        this.foodDelta = foodDelta;
        this.playerIdInSlot = playerIdInSlot;
    }

    public int getIndex() {return index;}

    public int getFoodDelta() {
        return foodDelta;
    }

    public String getPlayerId() {
        return playerIdInSlot;
    }

    public boolean isFree() {
        return playerIdInSlot == null;
    }

    public void placeTotem(String PlayerId) {
        this.playerIdInSlot = PlayerId;
    }

    public void clear() {
        this.playerIdInSlot = null;
    }

    //metodi per maggiore scorrevolezza del codice
    public boolean requiresPayment() {
        return foodDelta < 0;
    }

    public boolean givesFood() {
        return foodDelta > 0;
    }


}
