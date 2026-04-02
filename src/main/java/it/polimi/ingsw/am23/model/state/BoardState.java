package it.polimi.ingsw.am23.model.state;

import java.util.List;
import java.util.Objects;

public final class BoardState {

    private final List<CardState> topRow;
    private final List<CardState> bottomRow;
    private final List<CardState> topBuildings;
    private final List<CardState> bottomBuildings;
    private final List<OfferTileState> offerTiles;
    private final List<TurnOrderSlotState> turnOrderSlots;

    public BoardState(List<CardState> topRow,
                      List<CardState> bottomRow,
                      List<CardState> topBuildings,
                      List<CardState> bottomBuildings,
                      List<OfferTileState> offerTiles,
                      List<TurnOrderSlotState> turnOrderSlots) {
        this.topRow = List.copyOf(Objects.requireNonNull(topRow, "topRow is null"));
        this.bottomRow = List.copyOf(Objects.requireNonNull(bottomRow, "bottomRow is null"));
        this.topBuildings = List.copyOf(Objects.requireNonNull(topBuildings, "topBuildings is null"));
        this.bottomBuildings = List.copyOf(Objects.requireNonNull(bottomBuildings, "bottomBuildings is null"));
        this.offerTiles = List.copyOf(Objects.requireNonNull(offerTiles, "offerTiles is null"));
        this.turnOrderSlots = List.copyOf(Objects.requireNonNull(turnOrderSlots, "turnOrderSlots is null"));
    }

    public List<CardState> getTopRow() {
        return topRow;
    }

    public List<CardState> getBottomRow() {
        return bottomRow;
    }

    public List<CardState> getTopBuildings() {
        return topBuildings;
    }

    public List<CardState> getBottomBuildings() {
        return bottomBuildings;
    }

    public List<OfferTileState> getOfferTiles() {
        return offerTiles;
    }

    public List<TurnOrderSlotState> getTurnOrderSlots() {
        return turnOrderSlots;
    }
}
