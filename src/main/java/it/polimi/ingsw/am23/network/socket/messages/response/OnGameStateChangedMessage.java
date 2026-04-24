package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.model.state.GameState;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class OnGameStateChangedMessage extends Message {

    private final GameState gameState;

    public OnGameStateChangedMessage(GameState gameState) {
        this.gameState = gameState;
    }

    public GameState getGameState() { return gameState; }
}
