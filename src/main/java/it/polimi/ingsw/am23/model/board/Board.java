package it.polimi.ingsw.am23.model.board;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Board {

    private final CardMarket market;
    private final List<OfferTile> offerTiles;
    private final List<TurnOrderSlot>  turnOrderSlots;

    public Board(CardMarket market, List<OfferTile> offerTiles, List<TurnOrderSlot> turnOrderSlots) {
        this.market = Objects.requireNonNull(market, "market cannot be null");
        this.offerTiles = new ArrayList<>(Objects.requireNonNull(offerTiles, "offerTiles cannot be null"));
        this.turnOrderSlots = new ArrayList<>(Objects.requireNonNull(turnOrderSlots, "turnOrderSlots cannot be null"));
    }

    public CardMarket getMarket() {
        return market;
    }
    public List<OfferTile> getOfferTiles() {
        return offerTiles;
    }
    public List<TurnOrderSlot> getTurnOrderSlots() {
        return turnOrderSlots;
    }

    public TurnOrderSlot getTurnOrderSlot(int position) {
        return turnOrderSlots.get(position);
    }
    public OfferTile getOfferTile(int position) {
        return offerTiles.get(position);
    }

    public TurnOrderSlot findTurnOrderSlotOccupiedBy(String playerId) {
        Objects.requireNonNull(playerId, "playerId cannot be null");
        for (TurnOrderSlot slot : turnOrderSlots) {
            if(playerId.equals(slot.getOccupiedByPlayerId())) {
                return slot;
            }
        }
        return null;
    }
    public OfferTile findOfferTileOccupiedBy(String playerId) {
        Objects.requireNonNull(playerId, "playerId cannot be null");
        for (OfferTile tile : offerTiles) {
            if(playerId.equals(tile.getOccupiedByPlayerId())) {
                return tile;
            }
        }
        return null;
    }
    public List<OfferTile> getFreeOfferTiles() {
        List<OfferTile> freeOfferTiles = new ArrayList<>();
        for (OfferTile tile : offerTiles) {
            if(tile.isFree()) {
                freeOfferTiles.add(tile);
            }
        }
        return freeOfferTiles;

    }

    public List<TurnOrderSlot> getFreeTurnOrderSlots() {
        List<TurnOrderSlot> freeTurnOrderSlots = new ArrayList<>();
        for (TurnOrderSlot slot : turnOrderSlots) {
            if(slot.isFree()) {
                freeTurnOrderSlots.add(slot);
            }
        }
        return freeTurnOrderSlots;
    }

    public void clearOfferTiles(){
        for (OfferTile tile : offerTiles) {
            tile.clear();
        }
    }
    public void clearTurnOrderSlots(){
        for (TurnOrderSlot slot : turnOrderSlots) {
            slot.clear();
        }
    }



}
