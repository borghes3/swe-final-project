package it.polimi.ingsw.am23.controller;

import it.polimi.ingsw.am23.model.ErrorCode;
import it.polimi.ingsw.am23.model.resolvers.ScoreResult;
import it.polimi.ingsw.am23.model.state.GameState;

import java.util.List;

public interface GameView {
    void showGameState(GameState gameState);
    void showError(ErrorCode errorCode, String message);

    void showGameStarted(GameState gameState);
    void showEndOfPlacingPhase(GameState gameState);
    void showEndOfDrawingPhase(GameState gameState);
    void showExtraDrawRequest(GameState gameState);
    void showEndOfResolvingPhase(GameState gameState);
    void showEraProgression(GameState gameState);
    void showGameOver(GameState gameState);
    void showScoreboard(List<ScoreResult> scoreBoard);
    void showInfo(String message);
}