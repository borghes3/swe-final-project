package it.polimi.ingsw.am23.model.payloads;

import java.io.Serializable;

public record ExtraCardTakenPayload(String playerId, String cardId) implements Serializable {
}
