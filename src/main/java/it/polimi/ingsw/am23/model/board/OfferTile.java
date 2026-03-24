package it.polimi.ingsw.am23.model.board;

import java.util.Objects;

public class OfferTile {
    private final char id;
    private String occupiedByPlayerId;
    private final int minPlayers;
    private final OfferAction action;

    public OfferTile(char id, String occupiedByPlayerId, int minPlayers, OfferAction action) {
        this.id = id;
        this.occupiedByPlayerId = occupiedByPlayerId;
        this.minPlayers = minPlayers;
        this.action = Objects.requireNonNull(action);
    }

    public char getId() {
        return id;
    }

    public String getOccupiedByPlayerId() {
        return occupiedByPlayerId;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public OfferAction getAction() {
        return action;
    }

    public boolean isFree(){
        return occupiedByPlayerId == null;
    }

    public void placeTotem(String playerId){
        occupiedByPlayerId = playerId;
    }

    public void clear(){
        occupiedByPlayerId = null;
    }



}
