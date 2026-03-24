package it.polimi.ingsw.am23.model.state;

import java.util.Objects;


public final class OfferTileState {

    private final char tileId;
    private final CardState topCard;

    public OfferTileState(char tileId, CardState topCard) {
        this.tileId = tileId;
        this.topCard = topCard;
    }
}
