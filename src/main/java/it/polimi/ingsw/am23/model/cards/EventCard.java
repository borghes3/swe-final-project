package it.polimi.ingsw.am23.model.cards;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.EventCardState;

public abstract class EventCard extends Card {

    private final boolean isFinal;

    protected EventCard(String id, Era era, int points, boolean isFinal) {
        super(id, era, points);
        this.isFinal = isFinal;
    }

    @Override
    public boolean canBeTaken() {
        return false;
    }

    @Override
    public void onTaken(Game game, Player player) {
        throw new UnsupportedOperationException("Event cards cannot be taken");
    }

    public boolean isFinal() {
        return isFinal;
    }

    public abstract void resolve(Game game);

    @Override
    public CardState toState(){
        return new EventCardState(
                getId(),
                getEra(),
                getPoints()
        );
    }
}
