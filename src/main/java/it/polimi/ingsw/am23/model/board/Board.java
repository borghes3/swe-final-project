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

/**
 * Central game board.
 * Aggregates the list of {@link OfferTile} (the action offers players choose
 * during the placing phase) and the {@link TurnOrderTile} that drives the
 * next round's turn order. Also provides lookup helpers and the conversion
 * to a serializable {@link BoardState}.
 */
public class Board {

    private final List<OfferTile> offerTiles;
    private final TurnOrderTile turnOrderTile;

    /**
     * Builds a new board.
     *
     * @param offerTiles   ordered list of offer tiles, left-to-right
     * @param turnOrderTile the turn order tile
     */
    public Board(List<OfferTile> offerTiles, TurnOrderTile turnOrderTile) {
        this.offerTiles = new ArrayList<>(Objects.requireNonNull(offerTiles, "offerTiles cannot be null"));
        this.turnOrderTile = Objects.requireNonNull(turnOrderTile, "turnOrderTile cannot be null");
    }

    /** @return the turn order tile of this board */
    public TurnOrderTile getTurnOrderTile() {
        return turnOrderTile;
    }

    /**
     * Returns the offer tile identified by the supplied letter.
     *
     * @param id letter identifying the tile
     * @return the matching offer tile
     * @throws OfferTileNotFoundException if no offer tile carries the supplied id
     */
    public OfferTile getOfferTile(char id) {
        return offerTiles.stream().filter(t -> t.getId() == id).findFirst()
                .orElseThrow(() -> new OfferTileNotFoundException("Offer Tile with id " + id + " was not found on this board."));
    }

    /**
     * Returns the offer tile currently occupied by the supplied player.
     *
     * @param playerId id of the player
     * @return the offer tile occupied by the player
     * @throws OfferTileNotFoundException if no tile is occupied by that player
     */
    public OfferTile getOfferTileByPlayerId(String playerId) {
        for (OfferTile tile : offerTiles) {
            if (playerId.equals(tile.getOccupiedByPlayerId())) {
                return tile;
            }
        }
        throw new OfferTileNotFoundException("No Offer Tile is occupied by the given player id.");
    }

    /**
     * @return the first offer tile (leftmost) currently occupied, or
     * {@code null} when every tile is free
     */
    public OfferTile getFirstOccupiedOfferTile() {
        for (OfferTile tile : offerTiles) {
            if (!tile.isFree()) {
                return tile;
            }
        }
        return null;
    }

    /**
     * Returns the turn order slot occupied by the supplied player, if any.
     *
     * @param playerId id of the player to locate
     * @return the matching slot, or {@code null} if the player is not on the turn order tile
     */
    public TurnOrderSlot findTurnOrderSlotOccupiedBy(String playerId) {
        Objects.requireNonNull(playerId, "playerId cannot be null");
        for (TurnOrderSlot slot : turnOrderTile.getSlots()) {
            if (playerId.equals(slot.getPlayerId())) {
                return slot;
            }
        }
        return null;
    }

    /** @return the list of offer tiles that are currently free */
    public List<OfferTile> getFreeOfferTiles() {
        List<OfferTile> freeOfferTiles = new ArrayList<>();
        for (OfferTile tile : offerTiles) {
            if (tile.isFree()) {
                freeOfferTiles.add(tile);
            }
        }
        return freeOfferTiles;
    }

    /** @return the list of turn order slots that are currently free */
    public List<TurnOrderSlot> getFreeTurnOrderSlots() {
        List<TurnOrderSlot> freeTurnOrderSlots = new ArrayList<>();
        for (TurnOrderSlot slot : turnOrderTile.getSlots()) {
            if (slot.isFree()) {
                freeTurnOrderSlots.add(slot);
            }
        }
        return freeTurnOrderSlots;
    }

    /**
     * Builds the serializable snapshot of the whole board using the supplied
     * card market for the card rows.
     *
     * @param cardMarket the card market whose state should be embedded
     * @return the immutable board state
     */
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

    /** @return the per-tile serializable snapshot of all offer tiles */
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

    /** @return the per-slot serializable snapshot of the turn order tile */
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

    /** @return an unmodifiable copy of the offer tiles in board order */
    public List<OfferTile> getOfferTiles(){
        return List.copyOf(offerTiles);
    }

}
