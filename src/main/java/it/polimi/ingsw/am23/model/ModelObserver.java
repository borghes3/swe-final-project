package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.model.state.GameState;

public interface ModelObserver {

    void onGameStarted();

    void onGameStateChanged(GameState gameState);

    void onEndOfPlacingPhase(GameState gameState);

    void onGameOver();
}
