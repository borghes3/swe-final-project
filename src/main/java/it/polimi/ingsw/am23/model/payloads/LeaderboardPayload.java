package it.polimi.ingsw.am23.model.payloads;

import it.polimi.ingsw.am23.persistence.RankingEntry;

import java.io.Serializable;
import java.util.List;

public record LeaderboardPayload(int playerCount, List<RankingEntry> entries,
                                 boolean persistenceAvailable) implements Serializable {
}
