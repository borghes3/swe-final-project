package it.polimi.ingsw.am23.model.state;


import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.GamePhase;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class GameState implements Serializable {
    private final Era currentEra;
    private final int currentRound;
    private final GamePhase phase;
    private final String currentPlayerId;
    private final List<PlayerState> players;
    private final BoardState board;;



    public GameState(Era currentEra,
                     int currentRound,
                     GamePhase phase,
                     String currentPlayerId,
                     List<PlayerState> players,
                     BoardState board) {
        this.currentEra = Objects.requireNonNull(currentEra, "currentEra is null");
        this.currentRound = currentRound;
        this.phase = Objects.requireNonNull(phase, "phase is null");
        this.currentPlayerId = currentPlayerId; //può essere null in fase di setup
        this.players = List.copyOf(Objects.requireNonNull(players, "players is null")); //
        this.board = board;
    }

    public Era getCurrentEra() {
        return currentEra;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public String getCurrentPlayerId() {
        return currentPlayerId;
    }

    public List<PlayerState> getPlayers() {
        return players;
    }

    public BoardState getBoard() {
        return board;
    }


}
