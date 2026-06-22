package it.polimi.ingsw.am23.network.socket.messages.request;

import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class PlaceTotemMessage extends Message {

    private final String playerId;
    private final char offerTileChar;

    public PlaceTotemMessage(String playerId, char offerTileChar) {
        this.playerId = playerId;
        this.offerTileChar = offerTileChar;
    }

    public String getPlayerId() {
        return playerId;
    }

    public char getOfferTileChar() {
        return offerTileChar;
    }
}
