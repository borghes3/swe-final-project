package it.polimi.ingsw.am23.model.board;

import java.util.ArrayList;
import java.util.List;

public class RoundManager {
    private List<String> placingOrder;
    private List<String> resolvingOrder;        // Relativo all'ordine in cui si pescano le carte dal tracciato offerte
    private int placingIndex;
    private int resolvingIndex;

    public RoundManager(List<String> initialPlacingOrder) {
        if (initialPlacingOrder.isEmpty()) {
            throw new IllegalArgumentException("Placing order cannot be empty");
        }

        this.placingOrder = new ArrayList<>(initialPlacingOrder);
        this.resolvingOrder = new ArrayList<>();
        this.placingIndex = 0;
        this.resolvingIndex = 0;
    }

    public int getPlacingIndex() {
        return placingIndex;
    }

    public int getResolvingIndex() {
        return resolvingIndex;
    }

    public List<String> getPlacingOrder() {
        return placingOrder;
    }

    public List<String> getResolvingOrder() {
        return resolvingOrder;
    }


    public String getCurrentPlacingPlayerId() {
        if (isPlacingPhaseComplete()) {
            return null;
        }
        return placingOrder.get(placingIndex);
    }

    public String getCurrentResolvingPlayerId() {
        if (isResolvingPhaseComplete()) {
            return null;
        }
        return resolvingOrder.get(resolvingIndex);
    }

    public void advancePlacing() {
        if (!isPlacingPhaseComplete()) {
            placingIndex++;
        }
    }

    public void advanceResolving() {
        if (!isResolvingPhaseComplete()) {
            resolvingIndex++;
        }
    }

    public boolean isPlacingPhaseComplete() {
        return placingIndex >= placingOrder.size();
    }

    public boolean isResolvingPhaseComplete() {
        return resolvingIndex >= resolvingOrder.size();
    }

    public void setResolvingOrder(List<String> resolvingOrder) {
        this.resolvingOrder = new ArrayList<>(resolvingOrder);
        this.resolvingIndex = 0;
    }

    public void setNextRoundOrder(List<String> nextRoundOrder) {
        if (nextRoundOrder.isEmpty()) {
            throw new IllegalArgumentException("Next round order cannot be empty");
        }
        this.placingOrder = new ArrayList<>(nextRoundOrder);
        this.placingIndex = 0;
        this.resolvingOrder = new ArrayList<>();
        this.resolvingIndex = 0;
    }

    public void resetResolvingPhase() { //si può anche togliere in realtà ma per ora lo possiamo anche lasciare qui
        this.resolvingOrder = new ArrayList<>();
        this.resolvingIndex = 0;
    }
}
