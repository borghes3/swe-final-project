package it.polimi.ingsw.am23.model.payloads;

import java.io.Serializable;

public record TotemPlacedPayload(String playerId, char offerTileChar) implements Serializable {
}
