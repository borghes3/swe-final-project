package it.polimi.ingsw.am23.model.payloads;

import java.io.Serializable;
import java.util.List;

/**
 * Notification carrying the final scoreboard at the end of the match.
 *
 * @param scores per player score breakdown, ordered from highest to lowest
 */
public record ScoreBoardPayload(List<PlayerScore> scores) implements Serializable {
}
