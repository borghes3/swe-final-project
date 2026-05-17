package it.polimi.ingsw.am23.model.payloads;

import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.state.CardState;

import java.io.Serializable;
import java.util.List;

public record CardsTakenPayload(String playerId,
                                List<String> takenCardIds,
                                List<String> takenBuildingIds,
                                int foodSpentOnBuildings,
                                int foodGainedFromOfferTile,
                                int turnOrderSlotIndex,
                                int foodDeltaFromSlot,
                                GamePhase newPhase,
                                String nextPlayerId,List<CardState> takenCards,
                                List<CardState> takenBuildings, int absoluteFood,
                                boolean skipAllowed) implements Serializable {
}
