package it.polimi.ingsw.am23.model.payloads;

import it.polimi.ingsw.am23.model.enums.Era;

import java.util.List;

public record EventResolvedPayload(String eventCardId, Era era, List<PlayerDelta> playerDeltas) {
}
