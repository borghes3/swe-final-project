package it.polimi.ingsw.am23.controller;

import it.polimi.ingsw.am23.model.ActionResult;
import it.polimi.ingsw.am23.model.GameModel;
import it.polimi.ingsw.am23.model.ModelObserver;
import it.polimi.ingsw.am23.model.cards.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.model.cards.SelectedCards;
import it.polimi.ingsw.am23.model.payloads.*;
import it.polimi.ingsw.am23.model.state.GameState;
import java.util.Objects;

@Deprecated
public class LocalGameControllerDeprecated implements GameControllerDeprecated, ModelObserver {

    private final GameModel gameModel;
    private final GameView view;
    private final String localPlayerId;

    public LocalGameControllerDeprecated(GameModel gameModel, GameView view, String localPlayerId) {
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
    public void takeExtraCard(SelectedCardExtraDraw selectedCardExtraDraw) {
        handleResult(gameModel.takeExtraCard(localPlayerId, selectedCardExtraDraw));
    }

    @Override
    public void close() {
        gameModel.removeObserver(this);
    }

    @Override
    public void onGameStarted(GameStartedPayload payload) {
        GameState state = payload.fullSnapshot();
        if (state != null) {
            view.showGameStarted(state);
        } else {
            view.showInfo("Game started.");
        }
    }

    @Override
    public void onTotemPlaced(TotemPlacedPayload payload) {
        GameState state = gameModel.getGameState();
        if (state != null) {
            view.showGameState(state);
        }
    }

    @Override
    public void onEndOfPlacingPhase(EndOfPlacingPhasePayload payload) {
        GameState state = gameModel.getGameState();
        if (state != null) {
            view.showEndOfPlacingPhase(state);
        }
    }

    @Override
    public void onCardsTaken(CardsTakenPayload payload) {
        GameState state = gameModel.getGameState();
        if (state != null) {
            view.showEndOfDrawingPhase(state);
        }
    }

    @Override
    public void onExtraDrawRequest(ExtraDrawRequestPayload payload) {
        GameState state = gameModel.getGameState();
        if (state != null) {
            view.showExtraDrawRequest(state);
        }
    }

    @Override
    public void onExtraCardTaken(ExtraCardTakenPayload payload) {
        GameState state = gameModel.getGameState();
        if (state != null) {
            view.showGameState(state);
        }
    }

    @Override
    public void onEventResolved(EventResolvedPayload payload) {
        GameState state = gameModel.getGameState();
        if (state != null) {
            view.showEndOfResolvingPhase(state);
        }
    }

    @Override
    public void onEraProgression(EraProgressionPayload payload) {
        GameState state = gameModel.getGameState();
        if (state != null) {
            view.showEraProgression(state);
        }
    }

    @Override
    public void onMarketRefreshed(MarketRefresherPayload payload) {
        GameState state = gameModel.getGameState();
        if (state != null) {
            view.showGameState(state);
        }
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
    public void onScoreboardAvailable(ScoreBoardPayload payload) {
        view.showInfo("Scoreboard: " + payload.scores());
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