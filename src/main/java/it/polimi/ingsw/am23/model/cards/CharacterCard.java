package it.polimi.ingsw.am23.model.cards;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.CharacterCardState;

import java.util.Objects;

/**
 * Abstract base class for the cards representing tribe characters. Concrete
 * subclasses model the different archetypes (Hunter, Gatherer, Shaman, ...)
 * and may override {@link #onAddedToTribe(Game, Player)} to apply archetype
 * specific side effects when collected.
 */
public abstract class CharacterCard extends Card {
    private final CharacterType characterType;
    private final int minPlayers;

    /**
     * Initializes the shared character card fields.
     *
     * @param id            unique identifier of the card
     * @param era           era the card belongs to
     * @param points        printed victory points
     * @param characterType archetype represented by the card
     * @param minPlayers    minimum number of players for which the card is in play
     */
    protected CharacterCard(String id, Era era, int points, CharacterType characterType, int minPlayers) {
        super(id, era, points);
        this.characterType = Objects.requireNonNull(characterType);
        this.minPlayers = minPlayers;
    }

    /**
     * @return the archetype represented by this card
     */
    public CharacterType getCharacterType() {
        return characterType;
    }

    /**
     * @return the minimum number of players for which the card is in play
     */
    public int getMinPlayers() {
        return minPlayers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canBeTaken() {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>Adds the character to the player's tribe and forwards the event to
     * {@link #onAddedToTribe(Game, Player)} for subclass specific behavior.</p>
     */
    @Override
    public void onTaken(Game game, Player player) {
        Objects.requireNonNull(game);
        Objects.requireNonNull(player);

        player.getTribe().addCharacter(this);
        onAddedToTribe(game, player);
    }

    /**
     * Hook invoked after the character has been added to the player's tribe.
     * Default implementation is a no-op; subclasses can override to react.
     *
     * @param game   the game instance
     * @param player the player who collected the card
     */
    protected void onAddedToTribe(Game game, Player player) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardState toState() {
        return new CharacterCardState(
                getId(),
                getEra(),
                getPoints(),
                getCharacterType(),
                getMinPlayers(),
                null,
                null,
                null,
                null
        );
    }

    /**
     * Discount granted by this character when buying buildings.
     *
     * @return the food discount, defaulting to 0
     */
    public int getDiscount() {
        return 0;
    }
}
