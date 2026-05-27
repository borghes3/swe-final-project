package it.polimi.ingsw.am23.model.payloads;

import it.polimi.ingsw.am23.persistence.RankingEntry;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Sent at game end after the local scoreboard.
 * Carries each player's position in the global ranking of all matches
 * with the same number of players, plus a snapshot of the top entries
 * so the client can immediately show a teaser of the leaderboard.
 *
 * @param playerCount          number of players in the just-finished match
 * @param positionByPlayerId   map playerId -> 1-based position in the global ranking
 *                             (-1 = persistence offline)
 * @param topEntries           ranked snapshot of the top N entries for that player count
 * @param persistenceAvailable false = persistence/DB offline
 */
public record MatchRankingsPayload(
        int playerCount,
        Map<String, Integer> positionByPlayerId,
        List<RankingEntry> topEntries,
        boolean persistenceAvailable
) implements Serializable {
}
