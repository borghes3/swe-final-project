package it.polimi.ingsw.am23.model.payloads;

import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.state.CardState;

import java.io.Serializable;
import java.util.List;

/**
 * Notification sent when the game progresses to a new era and the building
 * row is refreshed accordingly.
 *
 * @param newEra                   the era the match has just entered
 * @param newBuildingIdsInUpperRow ids of the buildings now visible in the upper row
 * @param discardedBuildingIds     ids of buildings discarded as part of the progression
 * @param newBuildingCards         full state of the buildings now visible in the upper row
 */
public record EraProgressionPayload(Era newEra,
                                    List<String> newBuildingIdsInUpperRow,
                                    List<String> discardedBuildingIds,
                                    List<CardState> newBuildingCards) implements Serializable {
}
