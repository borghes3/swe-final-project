package it.polimi.ingsw.am23.persistence;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Immutable entry of the global leaderboard, ready to be serialized and
 * shipped to clients.
 *
 * @param id          database identifier of the entry
 * @param nickname    display nickname of the player
 * @param score       prestige points achieved
 * @param matchDate   end-of-match timestamp
 * @param playerCount number of players in the match
 * @param position    1-based position within the player-count slice
 */
public record RankingEntry(long id, String nickname, int score, LocalDateTime matchDate, int playerCount,
                           int position) implements Serializable {
}
