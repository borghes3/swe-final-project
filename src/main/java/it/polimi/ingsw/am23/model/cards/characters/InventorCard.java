package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.InventionIcon;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.CharacterCardState;

import java.util.Objects;

/**
 * Concrete {@link CharacterCard} representing the Inventor archetype.
 * Each Inventor exposes an icon that contributes to inventor-pair scoring.
 */
public class InventorCard extends CharacterCard {

    private final InventionIcon icon;

    /**
     * Builds a new Inventor card.
     *
     * @param id         unique identifier of the card
     * @param era        era the card belongs to
     * @param points     printed victory points
     * @param icon       invention icon depicted on the card
     * @param minPlayers minimum number of players for which the card is in play
     */
    public InventorCard(String id, Era era, int points, InventionIcon icon, int minPlayers) {
        super(id, era, points, CharacterType.INVENTOR, minPlayers);
        this.icon = Objects.requireNonNull(icon, "icon cannot be null");
    }

    /**
     * @return the invention icon depicted on the card
     */
    public InventionIcon getIcon() {
        return icon;
    }

    /**
     * {@inheritDoc}
     * <p>Registers the icon on the player's tribe so it can contribute to
     * inventor-pair effects.</p>
     */
    @Override
    public void onAddedToTribe(Game game, Player player) {
        player.getTribe().incrementInventorIconCount(getIcon());
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
                icon
        );
    }
}
