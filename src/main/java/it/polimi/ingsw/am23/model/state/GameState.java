package it.polimi.ingsw.am23.model.state;


import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.GamePhase;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Immutable serializable snapshot of the whole game. Carries the era, the
 * round, the current phase, the active player and the snapshot of every
 * player and of the board. Final scores are only attached after the game has
 * ended; clients can detect this via {@link #scores ()} returning non
 * {@code null}.
 */
public record GameState(Era currentEra, int currentRound, GamePhase phase, String currentPlayerId,
                        List<PlayerState> players, BoardState board, List<ScoreEntry> scores,
                        boolean skipAllowed) implements Serializable {
    /**
     * Builds a snapshot for the in-progress phases (no final scores yet).
     *
     * @param currentEra      era currently being played
     * @param currentRound    round number currently being played
     * @param phase           current game phase
     * @param currentPlayerId id of the active player, or {@code null} when none
     * @param players         per player snapshots
     * @param board           snapshot of the central board
     * @param skipAllowed     whether the active player can skip the draw
     */
    public GameState(Era currentEra,
                     int currentRound,
                     GamePhase phase,
                     String currentPlayerId,
                     List<PlayerState> players,
                     BoardState board,
                     boolean skipAllowed) {
        // may be null during setup
        this(Objects.requireNonNull(currentEra, "currentEra is null"), currentRound, Objects.requireNonNull(phase, "phase is null"), currentPlayerId, List.copyOf(Objects.requireNonNull(players, "players is null")), board, null, skipAllowed);
    }

    /**
     * Builds a snapshot for the terminal phase, attaching the final scores.
     * Use {@link #scores ()} non null to detect that this snapshot carries
     * the end of game score breakdown.
     *
     * @param currentEra      era at the end of the match
     * @param currentRound    round number at the end of the match
     * @param phase           current game phase (typically {@link GamePhase#ENDED})
     * @param currentPlayerId id of the active player, or {@code null} when none
     * @param players         per player snapshots
     * @param board           snapshot of the central board
     * @param scores          final score breakdown
     * @param skipAllowed     whether the active player can skip the draw
     */
    public GameState(Era currentEra,
                     int currentRound,
                     GamePhase phase,
                     String currentPlayerId,
                     List<PlayerState> players,
                     BoardState board,
                     List<ScoreEntry> scores,
                     boolean skipAllowed) {
        this.currentEra = Objects.requireNonNull(currentEra, "currentEra is null");
        this.currentRound = currentRound;
        this.phase = Objects.requireNonNull(phase, "phase is null");
        this.currentPlayerId = currentPlayerId; // may be null during setup
        this.players = List.copyOf(Objects.requireNonNull(players, "players is null"));
        this.board = board;
        this.scores = scores;
        this.skipAllowed = skipAllowed;
    }


    /**
     * @return the era currently being played
     */
    @Override
    public Era currentEra() {
        return currentEra;
    }

    /**
     * @return the current round number
     */
    @Override
    public int currentRound() {
        return currentRound;
    }

    /**
     * @return the current game phase
     */
    @Override
    public GamePhase phase() {
        return phase;
    }

    /**
     * @return id of the player whose turn is currently active, or {@code null}
     */
    @Override
    public String currentPlayerId() {
        return currentPlayerId;
    }

    /**
     * @return per player snapshots
     */
    @Override
    public List<PlayerState> players() {
        return players;
    }

    /**
     * @return the snapshot of the central board
     */
    @Override
    public BoardState board() {
        return board;
    }

    /**
     * @return the final score breakdown, or {@code null} if the game is still in progress
     */
    @Override
    public List<ScoreEntry> scores() {
        return scores;
    }

    /**
     * @return whether the active player can legally skip the draw
     */
    @Override
    public boolean skipAllowed() {
        return skipAllowed;
    }
}
