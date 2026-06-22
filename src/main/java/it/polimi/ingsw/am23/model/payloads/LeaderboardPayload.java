package it.polimi.ingsw.am23.model.payloads;

import it.polimi.ingsw.am23.persistence.RankingEntry;

import java.io.Serializable;
import java.util.List;

/**
 * Notification carrying the global leaderboard for matches with a given
 * number of players.
 *
 * @param playerCount          number of players the leaderboard refers to
 * @param entries              ranked list of entries (highest score first)
 * @param persistenceAvailable {@code false} when the persistence backend is offline
 */
public record LeaderboardPayload(int playerCount, List<RankingEntry> entries,
                                 boolean persistenceAvailable) implements Serializable {
}
