package it.polimi.ingsw.am23.model.cards;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.EventCardState;

/**
 * Abstract base class for the event cards. Event cards cannot be drawn by
 * players: they sit on the market until the end of the round, when they are
 * resolved by the {@link it.polimi.ingsw.am23.model.resolvers.EventResolver}.
 */
public abstract class EventCard extends Card {

    private final boolean isFinal;

    /**
     * Initializes the shared event card fields.
     *
     * @param id      unique identifier of the card
     * @param era     era the card belongs to
     * @param points  printed victory points
     * @param isFinal whether the card is resolved only at the end of the match
     */
    protected EventCard(String id, Era era, int points, boolean isFinal) {
        super(id, era, points);
        this.isFinal = isFinal;
    }

    /**
     * {@inheritDoc}
     *
     * @return always {@code false}: event cards can never be drawn
     */
    @Override
    public boolean canBeTaken() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException always, since event cards cannot be taken
     */
    @Override
    public void onTaken(Game game, Player player) {
        throw new UnsupportedOperationException("Event cards cannot be taken");
    }

    /**
     * @return {@code true} if this card resolves only at the end of the match
     */
    public boolean isFinal() {
        return isFinal;
    }

    /**
     * Resolves the event against the supplied game instance.
     *
     * @param game game instance to apply the event to
     */
    public abstract void resolve(Game game);

    /** {@inheritDoc} */
    @Override
    public CardState toState(){
        return new EventCardState(
                getId(),
                getEra(),
                getPoints()
        );
    }

    /**
     * Resolution priority used to determine the order in which multiple
     * event cards triggered together are evaluated; higher values resolve
     * first.
     *
     * @return the priority, defaulting to 0
     */
    public int getResolutionPriority() {
        return 0;
    }
}
