package it.polimi.ingsw.am23.model.board;

import it.polimi.ingsw.am23.exceptions.OfferTileNotFoundException;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderTile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Board {

    private final List<OfferTile> offerTiles;
    private final TurnOrderTile turnOrderTile;

    public Board(List<OfferTile> offerTiles, TurnOrderTile turnOrderTile) {
        this.offerTiles = new ArrayList<>(Objects.requireNonNull(offerTiles, "offerTiles cannot be null"));
        this.turnOrderTile = Objects.requireNonNull(turnOrderTile, "turnOrderTile cannot be null");
    }

    public TurnOrderTile getTurnOrderTile() {
        return turnOrderTile;
    }

    public OfferTile getOfferTile(char id) {
        return offerTiles.stream().filter(t -> t.getId() == id).findFirst()
                .orElseThrow(() -> new OfferTileNotFoundException("Offer Tile with id " + id + " was not found on this board."));
    }

    public OfferTile getOfferTileByPlayerId(String playerId) {
        for (OfferTile tile : offerTiles) {
            if (playerId.equals(tile.getOccupiedByPlayerId())) {
                return tile;
            }
        }
        throw new OfferTileNotFoundException("No Offer Tile is occupied by the given player id.");
    }

    public OfferTile getFirstOccupiedOfferTile() {
        for (OfferTile tile : offerTiles) {
            if (!tile.isFree()) {
                return tile;
            }
        }
        return null;
    }

    public TurnOrderSlot findTurnOrderSlotOccupiedBy(String playerId) {
        Objects.requireNonNull(playerId, "playerId cannot be null");
        for (TurnOrderSlot slot : turnOrderTile.getSlots()) {
            if (playerId.equals(slot.getPlayerId())) {
                return slot;
            }
        }
        return null;
    }

    public List<OfferTile> getFreeOfferTiles() {
        List<OfferTile> freeOfferTiles = new ArrayList<>();
        for (OfferTile tile : offerTiles) {
            if (tile.isFree()) {
                freeOfferTiles.add(tile);
            }
        }
        return freeOfferTiles;

    }

    public List<TurnOrderSlot> getFreeTurnOrderSlots() {
        List<TurnOrderSlot> freeTurnOrderSlots = new ArrayList<>();
        for (TurnOrderSlot slot : turnOrderTile.getSlots()) {
            if (slot.isFree()) {
                freeTurnOrderSlots.add(slot);
            }
        }
        return freeTurnOrderSlots;
    }

    public void clearOfferTiles() {
        for (OfferTile tile : offerTiles) {
            tile.clear();
        }
    }

    public void clearTurnOrderSlots() {
        for (TurnOrderSlot slot : turnOrderTile.getSlots()) {
            slot.clear();
        }
    }


}
