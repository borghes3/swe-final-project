package it.polimi.ingsw.am23.model.payloads;

import it.polimi.ingsw.am23.model.state.GameState;

public record GameStartedPayload(GameState fullSnapshot) {
}
