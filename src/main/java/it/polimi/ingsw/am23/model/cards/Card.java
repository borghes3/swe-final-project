package it.polimi.ingsw.am23.model.cards;

import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.state.CardState;

import java.util.Objects;

public abstract class Card {
    private final String id;
    private final Era era;
    private final int points;

    protected Card(String id, Era era, int points) {
        this.id = Objects.requireNonNull(id);
        this.era = Objects.requireNonNull(era);
        this.points = points;
    }

    public String getId() {
        return id;
    }
    public Era getEra() {
        return era;
    }

    public int getPoints() {
        return points;
    }

    public abstract boolean canBeTaken();

    public abstract void onTaken(Game game, Player player);

    public abstract CardState toState();
}
