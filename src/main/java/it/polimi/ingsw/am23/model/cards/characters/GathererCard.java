package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;

/**
 * Concrete {@link CharacterCard} representing the Gatherer archetype.
 * Gatherers contribute to scoring during the Sustenance event but have no
 * immediate effect when collected.
 */
public class GathererCard extends CharacterCard {
    /**
     * Builds a new Gatherer card.
     *
     * @param id         unique identifier of the card
     * @param era        era the card belongs to
     * @param points     printed victory points
     * @param minPlayers minimum number of players for which the card is in play
     */
    public GathererCard(String id, Era era, int points, int minPlayers) {
        super(id, era, points, CharacterType.GATHERER,  minPlayers);
    }
}
