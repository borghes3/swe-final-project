package it.polimi.ingsw.am23.model.board;

import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderTile;
import it.polimi.ingsw.am23.model.state.BoardState;
import it.polimi.ingsw.am23.model.state.OfferTileState;
import it.polimi.ingsw.am23.model.state.TurnOrderSlotState;

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

    public BoardState getState(){
        return new BoardState(market.getTopRow().stream().map(c -> c.toState()).toList(),
                market.getBottomRow().stream().map(c->c.toState()).toList(),
                market.getTopBuildings().stream().map(b ->b.toState()).toList(),
                market.getBottomBuildings().stream().map(b -> b.toState()).toList(),
                buildOfferTileState(),
                buildTurnOrderSlotsState()
        );
    }

    public List<OfferTileState> buildOfferTileState(){
        List<OfferTileState> offerTileStates = new ArrayList<>();
        for(int i = 0; i < offerTiles.size(); i++){
            OfferTile tile  = offerTiles.get(i);
            offerTileStates.add(new OfferTileState(
                    i,
                    tile.getId(),
                    tile.getOccupiedByPlayerId(),
                    tile.getMinPlayers(),
                    tile.getAction().getUpperDrawRowCount(),
                    tile.getAction().getBottomDrawCount(),
                    tile.getAction().getFoodReward()
            ));
        }
        return offerTileStates;
    }

    public List<TurnOrderSlotState> buildTurnOrderSlotsState(){
        List<TurnOrderSlotState> states = new ArrayList<>();
        List<TurnOrderSlot> slots = turnOrderTile.getSlots();

        for(int i = 0; i < slots.size(); i++){
            TurnOrderSlot slot = slots.get(i);
            states.add(new TurnOrderSlotState(
                    i,
                    slot.getFoodDelta(),
                    slot.getPlayerId()
            ));
        }
    }



}
