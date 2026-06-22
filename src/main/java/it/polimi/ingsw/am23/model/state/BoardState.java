package it.polimi.ingsw.am23.model.state;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of the central board, suitable for sending across the
 * network. Includes the card market rows, the building rows, the offer tiles
 * and the turn order slots.
 *
 * @param topRow          state of the cards in the top row of the market
 * @param bottomRow       state of the cards in the bottom row of the market
 * @param topBuildings    state of the buildings in the top row
 * @param bottomBuildings state of the buildings in the bottom row
 * @param offerTiles      state of all offer tiles
 * @param turnOrderSlots  state of all turn order slots
 */
public record BoardState(List<CardState> topRow, List<CardState> bottomRow, List<CardState> topBuildings,
                         List<CardState> bottomBuildings, List<OfferTileState> offerTiles,
                         List<TurnOrderSlotState> turnOrderSlots) implements Serializable {
    /**
     * Builds a new immutable snapshot of the board.
     *
     * @param topRow          state of the cards in the top row of the market
     * @param bottomRow       state of the cards in the bottom row of the market
     * @param topBuildings    state of the buildings in the top row
     * @param bottomBuildings state of the buildings in the bottom row
     * @param offerTiles      state of all offer tiles
     * @param turnOrderSlots  state of all turn order slots
     */
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

    /**
     * Returns the cards visible in the top market row.
     *
     * @return the cards currently visible in the top market row
     */
    @Override
    public List<CardState> topRow() {
        return topRow;
    }

    /**
     * Returns the cards visible in the bottom market row.
     *
     * @return the cards currently visible in the bottom market row
     */
    @Override
    public List<CardState> bottomRow() {
        return bottomRow;
    }

    /**
     * Returns the buildings visible in the top building row.
     *
     * @return the buildings currently visible in the top building row
     */
    @Override
    public List<CardState> topBuildings() {
        return topBuildings;
    }

    /**
     * Returns the buildings visible in the bottom building row.
     *
     * @return the buildings currently visible in the bottom building row
     */
    @Override
    public List<CardState> bottomBuildings() {
        return bottomBuildings;
    }

    /**
     * Returns the snapshot of every offer tile on the board, in board order.
     *
     * @return the snapshot of every offer tile on the board
     */
    @Override
    public List<OfferTileState> offerTiles() {
        return offerTiles;
    }

    /**
     * Returns the snapshot of every turn order slot, in slot order.
     *
     * @return the snapshot of every turn order slot
     */
    @Override
    public List<TurnOrderSlotState> turnOrderSlots() {
        return turnOrderSlots;
    }
}
