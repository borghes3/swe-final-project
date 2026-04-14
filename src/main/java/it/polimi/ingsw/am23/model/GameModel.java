package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.model.cards.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.model.cards.SelectedCards;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.state.GameState;

// --------------------------------------------
// INTERFACCIA MODEL-CONTROLLER
// --------------------------------------------
//   [CONTROLLER] <------ (*)[MODEL]
//                         ^
//                         |
//      Questa interfaccia sta qui
// --------------------------------------------

public interface GameModel {

    ActionResult placeTotem(String playerId, char offerTileChar);

    ActionResult takeCards(String playerId, SelectedCards selectedCards);

    ActionResult takeExtraCard(String playerId, SelectedCardExtraDraw selectedCardExtraDraw);

    ActionResult resolveEvents();

    ActionResult calculateScores();

    GameState getGameState();

    GamePhase getGamePhase();

    void addObserver(ModelObserver observer);

    void removeObserver(ModelObserver observer);
}