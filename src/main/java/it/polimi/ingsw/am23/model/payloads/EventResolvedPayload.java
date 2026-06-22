package it.polimi.ingsw.am23.model.payloads;

import it.polimi.ingsw.am23.model.enums.Era;

import java.io.Serializable;
import java.util.List;

/**
 * Notification carrying the outcome of a single event card resolution.
 *
 * @param eventCardId  id of the event card that has just been resolved
 * @param era          era in which the event has been resolved
 * @param playerDeltas per player food and prestige deltas caused by the event
 */
public record EventResolvedPayload(String eventCardId, Era era,
                                   List<PlayerDelta> playerDeltas) implements Serializable {
}
