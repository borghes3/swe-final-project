package it.polimi.ingsw.am23.model.board;

public class TurnOrderSlot {

    private final int position;
    private final int foodDelta; //il delta di punti è sempre multiplo di foodDelta in teoria
    private String occupiedByPlayerId;

    public TurnOrderSlot(int position, int foodDelta, String occupiedByPlayerId) {
        this.position = position;
        this.foodDelta = foodDelta;
        this.occupiedByPlayerId = occupiedByPlayerId;
    }

    public int getPosition() {
        return position;
    }
    public int getFoodDelta() {
        return foodDelta;
    }
    public String getOccupiedByPlayerId() {
        return occupiedByPlayerId;
    }
    public boolean isFree(){
        return occupiedByPlayerId == null;
    }
    public void placeTotem(String PlayerId){
        this.occupiedByPlayerId = PlayerId;
    }
    public void clear(){
        this.occupiedByPlayerId = null;
    }

    //metodi per maggiore scorrevolezza del codice
    public boolean requiresPayment(){
        return foodDelta < 0;
    }
    public boolean givesFood(){
        return foodDelta > 0;
    }


}
