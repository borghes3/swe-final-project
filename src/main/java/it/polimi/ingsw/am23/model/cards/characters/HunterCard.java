package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.CharacterCardState;

/**
 * Concrete {@link CharacterCard} representing the Hunter archetype.
 * When a Hunter card sporting the food symbol is collected, the player gains
 * one food per Hunter already present in their tribe.
 */
public class HunterCard extends CharacterCard {

    private final boolean hasFoodSymbol;

    /**
     * Builds a new Hunter card.
     *
     * @param id            unique identifier of the card
     * @param era           era the card belongs to
     * @param points        printed victory points
     * @param hasFoodSymbol whether the card carries the food symbol
     * @param minPlayers    minimum number of players for which the card is in play
     */
    public HunterCard(String id, Era era, int points, boolean hasFoodSymbol, int minPlayers) {
        super(id, era, points, CharacterType.HUNTER, minPlayers);
        this.hasFoodSymbol = hasFoodSymbol;
    }

    /**
     * {@inheritDoc}
     * <p>If the card has the food symbol, awards one food per Hunter
     * already present in the player's tribe.</p>
     */
    @Override
    public void onAddedToTribe(Game game, Player player) {
        if (hasFoodSymbol) {
            int huntersInTribe = player.getTribe().count(CharacterType.HUNTER);
            player.applyFoodDelta(huntersInTribe);
        }
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
                hasFoodSymbol,
                null,
                null,
                null
        );
    }
}
