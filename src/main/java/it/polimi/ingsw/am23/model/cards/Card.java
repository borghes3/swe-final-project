package it.polimi.ingsw.am23.model.cards;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.state.CardState;

import java.util.Objects;

/**
 * Abstract base class for every card playable in the game.
 * Defines the shared identity ({@link #getId()}), the {@link Era} the card
 * belongs to, the printed points, and the hooks subclasses implement to
 * react to being drawn and to expose their serializable state.
 */
public abstract class Card {
    private final String id;
    private final Era era;
    private final int points;

    /**
     * Initializes the common card fields.
     *
     * @param id     unique identifier of the card
     * @param era    era the card belongs to
     * @param points printed victory points
     */
    protected Card(String id, Era era, int points) {
        this.id = Objects.requireNonNull(id);
        this.era = Objects.requireNonNull(era);
        this.points = points;
    }

    /**
     * @return the unique identifier of this card
     */
    public String getId() {
        return id;
    }

    /**
     * @return the era the card belongs to
     */
    public Era getEra() {
        return era;
    }

    /**
     * @return the printed victory points of the card
     */
    public int getPoints() {
        return points;
    }

    /**
     * Tells whether this card can be picked from the card market during a
     * normal draw action.
     *
     * @return {@code true} if the card can be drawn, {@code false} otherwise
     */
    public abstract boolean canBeTaken();

    /**
     * Hook invoked when the card is picked from the market by a player.
     *
     * @param game   the game instance the card belongs to
     * @param player the player who picked the card
     */
    public abstract void onTaken(Game game, Player player);

    /**
     * Builds the serializable snapshot used to notify the clients.
     *
     * @return the serializable view of the card
     */
    public abstract CardState toState();
}
