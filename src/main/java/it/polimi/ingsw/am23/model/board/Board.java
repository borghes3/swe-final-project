package it.polimi.ingsw.am23.model.board;

import it.polimi.ingsw.am23.exceptions.OfferTileNotFoundException;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderTile;
import it.polimi.ingsw.am23.model.state.BoardState;
import it.polimi.ingsw.am23.model.state.OfferTileState;
import it.polimi.ingsw.am23.model.state.TurnOrderSlotState;

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

    public BoardState getState(CardMarket cardMarket) {
        Objects.requireNonNull(cardMarket, "cardMarket cannot be null");

        return new BoardState(
                cardMarket.getTopRow().stream().map(Card::toState).toList(),
                cardMarket.getBottomRow().stream().map(Card::toState).toList(),
                cardMarket.getTopBuildings().stream().map(BuildingCard::toState).toList(),
                cardMarket.getBottomBuildings().stream().map(BuildingCard::toState).toList(),
                buildOfferTileState(),
                buildTurnOrderSlotsState()
        );
    }

    public List<OfferTileState> buildOfferTileState() {
        List<OfferTileState> offerTileStates = new ArrayList<>();
        for (int i = 0; i < offerTiles.size(); i++) {
            OfferTile tile = offerTiles.get(i);
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

    public List<TurnOrderSlotState> buildTurnOrderSlotsState() {
        List<TurnOrderSlotState> states = new ArrayList<>();
        List<TurnOrderSlot> slots = turnOrderTile.getSlots();

        for (int i = 0; i < slots.size(); i++) {
            TurnOrderSlot slot = slots.get(i);
            states.add(new TurnOrderSlotState(
                    i,
                    slot.getFoodDelta(),
                    slot.getPlayerId()
            ));
        }
        return states;
    }

}
