package it.polimi.ingsw.am23.model.payloads;

import it.polimi.ingsw.am23.model.state.GameState;

import java.io.Serializable;

/**
 * Notification dispatched right after the game starts, carrying the initial
 * snapshot of the model so clients can render the starting state.
 *
 * @param fullSnapshot complete game state at the moment of the start
 */
public record GameStartedPayload(GameState fullSnapshot) implements Serializable {
}
