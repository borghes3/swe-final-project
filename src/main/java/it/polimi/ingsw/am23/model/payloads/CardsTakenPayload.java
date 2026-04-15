package it.polimi.ingsw.am23.model.payloads;

import java.util.List;

public record CardsTakenPayload(String playerId, List<String> takenCardIds, List<String> takenBuildingIds,
                                int foodSpentOnBuildings, int foodGainedFromOfferTile, int turnOrderSlotIndex, int fooddeltaFromSlot) {
}
