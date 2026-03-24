package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.enums.RowType;
import it.polimi.ingsw.am23.model.state.GameState;

public interface GameModel {
    ActionResult placeTotem(String playerId, int offerTilePosition);
    ActionResult takeCard(String playerId, RowType row, int index);
    ActionResult buildBuilding(String playerId, String buildingId);
    ActionResult takeExtraCard(String playerId, int index);

    GameState getGameState();
    String getCurrentPlayerId();
    GamePhase getGamePhase();
}
