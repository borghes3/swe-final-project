package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.model.state.GameState;

public interface ModelObserver {
    void onGameStarted(GameState gameState);
    void onGameStateChanged(GameState gameState);
    void onEndOfPlacingPhase(GameState gameState);
    void onEndOfDrawingPhase(GameState gameState);
    void onExtraDrawRequest(GameState gameState);
    void onEventResolved(GameState gameState);
    void onEraProgression(GameState gameState);
    void onGameOver(GameState gameState);
    void onScoreboardAvailable(GameState gameState); // qua gameState.getScores() != null
}
