package it.polimi.ingsw.am23.model.cards.turnorder;

import it.polimi.ingsw.am23.exceptions.NoFreeSlotsException;

import java.util.List;

public class TurnOrderTile {
    private final List<TurnOrderSlot> slots;

    public TurnOrderTile(List<TurnOrderSlot> slots) {
        this.slots = slots;
    }

    public int getSlotsCount() {
        return slots.size();
    }

    public List<TurnOrderSlot> getSlots() {
        return slots;
    }

    public TurnOrderSlot getSlot(int index) {
        return slots.get(index);
    }

    public int getSlotIndex(TurnOrderSlot target) {
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i) == target) {
                return i;
            }
        }
        throw new IllegalArgumentException("Slot not found in this TurnOrderTile");
    }

    public TurnOrderSlot getFirstFreeSlot() {
        for (TurnOrderSlot slot : slots) {
            if (slot.isFree()) {
                return slot;
            }
        }
        throw new NoFreeSlotsException("There are no free slots in this Turn Order Card");
    }

    public TurnOrderSlot getFirstOccupiedSlot() {
        for (TurnOrderSlot slot : slots) {
            if (!slot.isFree()) {
                return slot;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        for (TurnOrderSlot slot : slots) {
            if (!slot.isFree()) {
                return false;
            }
        }
        return true;
    }

}
