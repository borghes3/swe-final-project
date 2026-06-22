package it.polimi.ingsw.am23.model.board;

import java.util.Objects;

/**
 * One of the offer tiles on the central board. Each tile has a stable
 * letter identifier, an attached {@link OfferAction} describing the draws
 * and reward granted on resolution, and may host one player's totem at a
 * time.
 */
public class OfferTile {
    private final char id;
    private final int minPlayers;
    private final OfferAction action;
    private String occupiedByPlayerId;

    /**
     * Builds a new offer tile.
     *
     * @param id                 letter identifying the tile
     * @param occupiedByPlayerId id of the player initially on the tile, or {@code null}
     * @param minPlayers         minimum number of players for which the tile is in play
     * @param action             draws and reward granted upon resolution
     */
    public OfferTile(char id, String occupiedByPlayerId, int minPlayers, OfferAction action) {
        this.id = id;
        this.occupiedByPlayerId = occupiedByPlayerId;
        this.minPlayers = minPlayers;
        this.action = Objects.requireNonNull(action);
    }

    /**
     * @return the letter identifying this tile
     */
    public char getId() {
        return id;
    }

    /**
     * @return the id of the player currently on the tile, or {@code null}
     */
    public String getOccupiedByPlayerId() {
        return occupiedByPlayerId;
    }

    /**
     * @return the minimum number of players for which this tile is in play
     */
    public int getMinPlayers() {
        return minPlayers;
    }

    /**
     * @return the action descriptor of this tile
     */
    public OfferAction getAction() {
        return action;
    }

    /**
     * @return {@code true} if no player is on this tile
     */
    public boolean isFree() {
        return occupiedByPlayerId == null;
    }

    /**
     * Places a player's totem on the tile.
     *
     * @param playerId id of the player to place
     */
    public void placeTotem(String playerId) {
        occupiedByPlayerId = playerId;
    }

    /**
     * Removes the totem currently on the tile, if any.
     */
    public void clear() {
        occupiedByPlayerId = null;
    }

}
