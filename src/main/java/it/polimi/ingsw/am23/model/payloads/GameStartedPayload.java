package it.polimi.ingsw.am23.model.payloads;

import it.polimi.ingsw.am23.model.state.GameState;

import java.io.Serializable;

public record GameStartedPayload(GameState fullSnapshot) implements Serializable {
}
