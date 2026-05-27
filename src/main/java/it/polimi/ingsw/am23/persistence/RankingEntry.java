package it.polimi.ingsw.am23.persistence;

import java.io.Serializable;
import java.time.LocalDateTime;

public record RankingEntry(long id, String nickname, int score, LocalDateTime matchDate, int playerCount,
                           int position) implements Serializable {
}
