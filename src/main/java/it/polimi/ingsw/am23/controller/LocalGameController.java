package it.polimi.ingsw.am23.controller;

import it.polimi.ingsw.am23.model.ActionResult;
import it.polimi.ingsw.am23.model.GameModel;
import it.polimi.ingsw.am23.model.ModelObserver;
import it.polimi.ingsw.am23.model.cards.SelectedCards;
import it.polimi.ingsw.am23.model.state.GameState;

import java.util.Objects;

public class LocalGameController implements GameController, ModelObserver {

    private final GameModel gameModel;
    private final GameView view;
    private final String localPlayerId;

    public LocalGameController(GameModel gameModel, GameView view, String localPlayerId) {
        this.gameModel = Objects.requireNonNull(gameModel, "gameModel cannot be null");
        this.view = Objects.requireNonNull(view, "view cannot be null");
        this.localPlayerId = Objects.requireNonNull(localPlayerId, "localPlayerId cannot be null");

        this.gameModel.addObserver(this);

        GameState initialState = this.gameModel.getGameState();
        if (initialState != null) {
            view.showGameState(initialState);
        }
    }

    @Override
    public void placeTotem(char offerTileChar) {
        handleResult(gameModel.placeTotem(localPlayerId, offerTileChar));
    }

    @Override
    public void takeCards(SelectedCards selectedCards) {
        handleResult(gameModel.takeCards(localPlayerId, selectedCards));
    }

    @Override
    public void takeExtraCard(int index) {
        handleResult(gameModel.takeExtraCard(localPlayerId, index));
    }

    @Override
    public void close() {
        gameModel.removeObserver(this);
    }

    @Override
    public void onGameStarted() {
        GameState state = gameModel.getGameState();
        if (state != null) {
            view.showGameStarted(state);
        } else {
            view.showInfo("Game started.");
        }
    }

    @Override
    public void onGameStateChanged(GameState gameState) {
        view.showGameState(gameState);
    }

    @Override
    public void onEndOfPlacingPhase(GameState gameState) {
        view.showEndOfPlacingPhase(gameState);
    }

    @Override
    public void onEndOfDrawingPhase(GameState gameState) {
        view.showEndOfDrawingPhase(gameState);
    }

    @Override
    public void onExtraDrawRequest(GameState gameState) {
        view.showExtraDrawRequest(gameState);
    }

    @Override
    public void onEndOfResolvingPhase(GameState gameState) {
        view.showEndOfResolvingPhase(gameState);
    }

    @Override
    public void onEraProgression(GameState gameState) {
        view.showEraProgression(gameState);
    }

    @Override
    public void onGameOver() {
        GameState state = gameModel.getGameState();
        if (state != null) {
            view.showGameOver(state);
        } else {
            view.showInfo("Game over.");
        }
    }

    @Override
    public void onScoreboardAvailable() {
        view.showScoreboard();
    }

    private void handleResult(ActionResult result) {
        if (result == null) {
            view.showInfo("No result returned.");
            return;
        }

        if (!result.isSuccess()) {
            view.showError(result.getError(), result.getMessage());
        }
    }
}