package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.CharacterCardState;


/**
 * Concrete {@link CharacterCard} representing the Shaman archetype.
 * Each Shaman contributes a number of stars to the player's track, used to
 * compute Shaman ritual scoring.
 */
public class ShamanCard extends CharacterCard {

    private final int stars;

    /**
     * Builds a new Shaman card.
     *
     * @param id         unique identifier of the card
     * @param era        era the card belongs to
     * @param points     printed victory points
     * @param stars      number of stars contributed to the Shaman track
     * @param minPlayers minimum number of players for which the card is in play
     */
    public ShamanCard(String id, Era era, int points, int stars, int minPlayers) {
        super(id, era, points, CharacterType.SHAMAN, minPlayers);
        this.stars = stars;
    }

    /**
     * @return the number of stars contributed to the Shaman track
     */
    public int getStars() {
        return stars;
    }

    /**
     * {@inheritDoc}
     * <p>Adds the card's stars to the player's Shaman track.</p>
     */
    @Override
    public void onAddedToTribe(Game game, Player player) {
        player.getTribe().addShamanStars(getStars());
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
                stars,
                null,
                null
        );
    }
}
