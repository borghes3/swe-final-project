package it.polimi.ingsw.am23.model.payloads;

import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.state.CardState;

import java.io.Serializable;
import java.util.List;

/**
 * Notification sent after a draw action, describing which cards the player
 * took, their economic impact and the state of the game right after the
 * action.
 *
 * @param playerId                id of the player who performed the draw
 * @param takenCardIds            ids of the character/event cards drawn
 * @param takenBuildingIds        ids of the building cards purchased
 * @param foodSpentOnBuildings    total food paid for the purchased buildings
 * @param foodGainedFromOfferTile food gained from the offer tile reward
 * @param turnOrderSlotIndex      index of the turn order slot where the player ended up
 * @param foodDeltaFromSlot       food delta granted or charged by that slot
 * @param newPhase                game phase after this action
 * @param nextPlayerId            id of the next player due to act
 * @param takenCards              full state of the character/event cards drawn
 * @param takenBuildings          full state of the building cards purchased
 * @param absoluteFood            player's total food after the action
 * @param skipAllowed             whether the next player can legally skip
 */
public record CardsTakenPayload(String playerId,
                                List<String> takenCardIds,
                                List<String> takenBuildingIds,
                                int foodSpentOnBuildings,
                                int foodGainedFromOfferTile,
                                int turnOrderSlotIndex,
                                int foodDeltaFromSlot,
                                GamePhase newPhase,
                                String nextPlayerId, List<CardState> takenCards,
                                List<CardState> takenBuildings, int absoluteFood,
                                boolean skipAllowed) implements Serializable {
}
