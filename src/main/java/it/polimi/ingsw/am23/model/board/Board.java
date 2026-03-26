package it.polimi.ingsw.am23.model.board;

import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderTile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Board {

    private final CardMarket market;
    private final List<OfferTile> offerTiles;
    private final TurnOrderTile  turnOrderTile;

    public Board(CardMarket market, List<OfferTile> offerTiles, TurnOrderTile turnOrderTile) {
        this.market = Objects.requireNonNull(market, "market cannot be null");
        this.offerTiles = new ArrayList<>(Objects.requireNonNull(offerTiles, "offerTiles cannot be null"));
        this.turnOrderTile = Objects.requireNonNull(turnOrderTile, "turnOrderTile cannot be null");
    }

    public CardMarket getMarket() {
        return market;
    }
    public List<OfferTile> getOfferTiles() {
        return offerTiles;
    }
    public TurnOrderTile getTurnOrderTile() {
        return turnOrderTile;
    }

    public TurnOrderSlot getTurnOrderSlot(int index) {
        return turnOrderTile.getSlot(index);
    }
    public OfferTile getOfferTile(int position) {
        return offerTiles.get(position);
    }

    public TurnOrderSlot findTurnOrderSlotOccupiedBy(String playerId) {
        Objects.requireNonNull(playerId, "playerId cannot be null");
        for (TurnOrderSlot slot : turnOrderTile.getSlots()) {
            if(playerId.equals(slot.getPlayerId())) {
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
        for (TurnOrderSlot slot : turnOrderTile.getSlots()) {
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
        for (TurnOrderSlot slot : turnOrderTile.getSlots()) {
            slot.clear();
        }
    }



}
