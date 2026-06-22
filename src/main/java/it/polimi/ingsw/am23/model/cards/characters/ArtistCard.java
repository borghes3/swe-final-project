package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;

/**
 * Concrete {@link CharacterCard} representing the Artist archetype.
 * Artists do not apply any side effect on collection: their contribution is
 * accounted for at scoring time through building effects.
 */
public class ArtistCard extends CharacterCard {
    /**
     * Builds a new Artist card.
     *
     * @param id         unique identifier of the card
     * @param era        era the card belongs to
     * @param points     printed victory points
     * @param minPlayers minimum number of players for which the card is in play
     */
    public ArtistCard(String id, Era era, int points, int minPlayers) {
        super(id, era, points, CharacterType.ARTIST, minPlayers);
    }
}
