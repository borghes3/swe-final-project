package it.polimi.ingsw.am23.model.cards.turnorder;

import it.polimi.ingsw.am23.exceptions.NoFreeSlotsException;

import java.util.List;

/**
 * The turn order tile: an ordered list of {@link TurnOrderSlot} on which
 * players are placed to determine the round's turn order. Provides
 * convenience lookups for the first free or occupied slot.
 */
public class TurnOrderTile {
    private final List<TurnOrderSlot> slots;

    /**
     * Builds a new turn order tile.
     *
     * @param slots ordered list of slots; the order defines the turn order
     */
    public TurnOrderTile(List<TurnOrderSlot> slots) {
        this.slots = slots;
    }

    /** @return the total number of slots on the tile */
    public int getSlotsCount() {
        return slots.size();
    }

    /** @return the slots of this tile in declaration order */
    public List<TurnOrderSlot> getSlots() {
        return slots;
    }

    /**
     * Returns the slot at the supplied position.
     *
     * @param index 0-based slot index
     * @return the matching slot
     */
    public TurnOrderSlot getSlot(int index) {
        return slots.get(index);
    }

    /**
     * Returns the index of the supplied slot on this tile.
     *
     * @param target slot to locate (looked up by reference)
     * @return the 0-based index of the slot on the tile
     * @throws IllegalArgumentException if {@code target} does not belong to this tile
     */
    public int getSlotIndex(TurnOrderSlot target) {
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i) == target) {
                return i;
            }
        }
        throw new IllegalArgumentException("Slot not found in this TurnOrderTile");
    }

    /**
     * @return the first free slot, scanning from the lowest index
     * @throws NoFreeSlotsException if every slot is occupied
     */
    public TurnOrderSlot getFirstFreeSlot() {
        for (TurnOrderSlot slot : slots) {
            if (slot.isFree()) {
                return slot;
            }
        }
        throw new NoFreeSlotsException("There are no free slots in this Turn Order Card");
    }

    /**
     * @return the first occupied slot, or {@code null} if the tile is empty
     */
    public TurnOrderSlot getFirstOccupiedSlot() {
        for (TurnOrderSlot slot : slots) {
            if (!slot.isFree()) {
                return slot;
            }
        }
        return null;
    }

    /** @return {@code true} if no slot is currently occupied */
    public boolean isEmpty() {
        for (TurnOrderSlot slot : slots) {
            if (!slot.isFree()) {
                return false;
            }
        }
        return true;
    }

}
