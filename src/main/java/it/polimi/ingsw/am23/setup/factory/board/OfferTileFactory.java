package it.polimi.ingsw.am23.setup.factory.board;

import it.polimi.ingsw.am23.model.board.OfferAction;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.setup.definition.board.OfferTileDefinition;

import java.util.Objects;

public class OfferTileFactory {

    public OfferTile create(OfferTileDefinition definition) {
        Objects.requireNonNull(definition, "definition cannot be null");

        OfferAction action = new OfferAction(
                definition.getTopDrawCount(),
                definition.getBottomDrawCount(),
                definition.getFoodReward()
        );

        return new OfferTile(
                definition.getId(),
                null,
                definition.getMinPlayers(),
                action
        );
    }
}