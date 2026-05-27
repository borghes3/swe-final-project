package it.polimi.ingsw.am23.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class LeaderboardRepository {

    private static final String LOG_PREFIX = "[Leaderboard]";

    // DDL
    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS match_results (
              id BIGSERIAL PRIMARY KEY,
              nickname VARCHAR(255) NOT NULL,
              score INT NOT NULL,
              match_date TIMESTAMP NOT NULL,
              player_count INT NOT NULL
            )
            """;

    // INDEXES
    private static final String CREATE_INDEX_PC_SQL =
            "CREATE INDEX IF NOT EXISTS idx_player_count ON match_results (player_count)";

    private static final String CREATE_INDEX_PC_SCORE_SQL =
            "CREATE INDEX IF NOT EXISTS idx_pc_score ON match_results (player_count, score DESC)";

    private static final String INSERT_SQL =
            "INSERT INTO match_results (nickname, score, match_date, player_count) VALUES (?, ?, ?, ?)";


    // QUERIES
    private static final String SELECT_TOP_SQL =
            "SELECT id, nickname, score, match_date, player_count " +
                    "FROM match_results WHERE player_count = ? " +
                    "ORDER BY score DESC, match_date ASC LIMIT ?";

    private static final String COUNT_BETTER_SQL =
            "SELECT COUNT(*) FROM match_results WHERE player_count = ? AND score > ?";

    private final DatabaseConfig config;
    private volatile boolean available;

    public LeaderboardRepository(DatabaseConfig config) {
        this.config = config;
        this.available = config.isValid();
        if (!available) {
            System.out.println(LOG_PREFIX + " DB config missing: persistence will be disabled.");
        }
    }

    public boolean isAvailable() {
        return available;
    }

    /**
     * Connection and error handling
     */
    public synchronized void init() {
        if (!available) return;
        System.out.println(LOG_PREFIX + " Connecting to " + config.url() + " …");
        try {
            Class.forName(config.driverClass());
        } catch (ClassNotFoundException e) {
            System.out.println(LOG_PREFIX + " JDBC driver not found: persistence will be disabled.");
            available = false;
            return;
        }
        try (Connection c = connect();
             Statement st = c.createStatement()) {
            st.executeUpdate(CREATE_TABLE_SQL);
            st.executeUpdate(CREATE_INDEX_PC_SQL);
            st.executeUpdate(CREATE_INDEX_PC_SCORE_SQL);
            System.out.println(LOG_PREFIX + " DB ready.");
        } catch (SQLException e) {
            System.out.println(LOG_PREFIX + " DB unreachable (" + e.getMessage()
                    + "): persistance will be disabled, the game will continue without leaderboard persistance.");
            available = false;
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(config.url(), config.user(), config.password());
    }

    /**
     * Saves game result for a single player
     *
     * @param nickname
     * @param score
     * @param playerCount
     * @param matchDate
     */
    public synchronized void saveResult(String nickname, int score, int playerCount, LocalDateTime matchDate) {
        if (!available) return;
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(INSERT_SQL)) {
            ps.setString(1, nickname);
            ps.setInt(2, score);
            ps.setTimestamp(3, Timestamp.valueOf(matchDate));
            ps.setInt(4, playerCount);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(LOG_PREFIX + " save failed for player '" + nickname + "': " + e.getMessage());
        }
    }

    /**
     * Position of a game-score based on the number of players
     *
     * @param score
     * @param playerCount
     */
    public synchronized int positionOf(int score, int playerCount) {
        if (!available) return -1;
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(COUNT_BETTER_SQL)) {
            ps.setInt(1, playerCount);
            ps.setInt(2, score);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) + 1;
                }
            }
        } catch (SQLException e) {
            System.out.println(LOG_PREFIX + " positionOf query failed: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Top {@code limit} matches for the given player count, descending order.
     *
     * @param playerCount
     * @param limit
     */
    public synchronized List<RankingEntry> topForPlayerCount(int playerCount, int limit) {
        if (!available) return List.of();
        List<RankingEntry> result = new ArrayList<>();
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(SELECT_TOP_SQL)) {
            ps.setInt(1, playerCount);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                int pos = 1;
                int prevScore = Integer.MIN_VALUE;
                int prevPos = 1;
                while (rs.next()) {
                    int score = rs.getInt("score");
                    int actualPos = pos;
                    if (score == prevScore) {
                        actualPos = prevPos;
                    } else {
                        prevPos = pos;
                        prevScore = score;
                    }
                    result.add(new RankingEntry(
                            rs.getLong("id"),
                            rs.getString("nickname"),
                            score,
                            rs.getTimestamp("match_date").toLocalDateTime(),
                            rs.getInt("player_count"),
                            actualPos
                    ));
                    pos++;
                }
            }
        } catch (SQLException e) {
            System.out.println(LOG_PREFIX + " matches leaderboard query failed: " + e.getMessage());
        }
        return result;
    }
}
