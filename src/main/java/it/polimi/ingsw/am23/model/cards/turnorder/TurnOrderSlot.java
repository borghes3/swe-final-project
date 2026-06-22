package it.polimi.ingsw.am23.model.cards.turnorder;

/**
 * One of the slots of the turn order tile. Each slot has an index, a food
 * delta (positive = reward, negative = cost) and may host one player at a
 * time.
 */
public class TurnOrderSlot {

    private final int index;
    private final int foodDelta; // negative -> cost, positive -> reward
    private String playerIdInSlot;

    /**
     * Builds a new turn order slot.
     *
     * @param index           0-based position on the turn order tile
     * @param foodDelta       food delta produced when a player lands on the slot
     * @param playerIdInSlot  id of the player initially placed on the slot, or {@code null}
     */
    public TurnOrderSlot(int index, int foodDelta, String playerIdInSlot) {
        this.index = index;
        this.foodDelta = foodDelta;
        this.playerIdInSlot = playerIdInSlot;
    }

    /** @return the 0-based position of this slot on the turn order tile */
    public int getIndex() {return index;}

    /** @return the food delta produced when a player lands on this slot */
    public int getFoodDelta() {
        return foodDelta;
    }

    /** @return id of the player on the slot, or {@code null} if free */
    public String getPlayerId() {
        return playerIdInSlot;
    }

    /** @return {@code true} if no player is currently on this slot */
    public boolean isFree() {
        return playerIdInSlot == null;
    }

    /**
     * Places a player's totem on this slot, overwriting any previous owner.
     *
     * @param PlayerId id of the player to place on the slot
     */
    public void placeTotem(String PlayerId) {
        this.playerIdInSlot = PlayerId;
    }

    /** Removes the totem currently on the slot, if any. */
    public void clear() {
        this.playerIdInSlot = null;
    }

    /** @return {@code true} if landing on this slot costs food */
    public boolean requiresPayment() {
        return foodDelta < 0;
    }

    /** @return {@code true} if landing on this slot grants food */
    public boolean givesFood() {
        return foodDelta > 0;
    }


}
