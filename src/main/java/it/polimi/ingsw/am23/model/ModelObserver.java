package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.model.resolvers.ScoreResult;
import it.polimi.ingsw.am23.model.state.GameState;

import java.util.List;

// --------------------------------------------
// INTERFACCIA OSSERVATA DALLA VIRTUAL VIEW
// --------------------------------------------
//      [MODEL](*) ------> [VIRTUAL VIEW]
//              ^
//              |
//      Questa interfaccia sta qui
// --------------------------------------------

public interface ModelObserver {

    void onGameStarted();

    void onGameStateChanged(GameState gameState);

    void onEndOfPlacingPhase(GameState gameState);

    void onEndOfDrawingPhase(GameState gameState);

    void onExtraDrawRequest(GameState gameState);

    // Da qui si deduce: END OF ROUND
    void onEndOfResolvingPhase(GameState gameState);

    void onEraProgression(GameState gameState);

    void onGameOver();

    void onScores(List<ScoreResult> scoreBoard);
}
