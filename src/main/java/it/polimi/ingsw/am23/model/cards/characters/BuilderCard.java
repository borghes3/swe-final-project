package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.CharacterCardState;

/**
 * Concrete {@link CharacterCard} representing the Builder archetype.
 * Builders grant a food discount applied when the owning player purchases a
 * building card.
 */
public class BuilderCard extends CharacterCard{

    private final int discount;

    /**
     * Builds a new Builder card.
     *
     * @param id         unique identifier of the card
     * @param era        era the card belongs to
     * @param points     printed victory points
     * @param discount   food discount granted when buying buildings
     * @param minPlayers minimum number of players for which the card is in play
     */
    public BuilderCard(String id, Era era, int points, int discount, int minPlayers) {
        super(id, era, points, CharacterType.BUILDER, minPlayers);
        this.discount = discount;
    }

    /** {@inheritDoc} */
    @Override
    public int getDiscount() {
        return discount;
    }

    /** {@inheritDoc} */
    @Override
    public CardState toState(){
        return new CharacterCardState(
                getId(),
                getEra(),
                getPoints(),
                getCharacterType(),
                getMinPlayers(),
                null,
                null,
                discount,
                null
        );
    }
}
