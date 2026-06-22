package it.polimi.ingsw.am23.model.state;

import it.polimi.ingsw.am23.model.enums.CardKind;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.InventionIcon;

/**
 * Immutable snapshot of a character card. Subtype specific attributes are
 * exposed as nullable fields so the renderer can adapt to each variant.
 */
public final class CharacterCardState extends CardState {

    private final CharacterType characterType;
    private final int minPlayers;

    // Optional attributes for the various card types
    private final Boolean hasFoodSymbol;
    private final Integer stars;
    private final Integer discount;
    private final InventionIcon inventionIcon;

    /**
     * Builds a new character card snapshot.
     *
     * @param cardId        unique identifier of the card
     * @param era           era the card belongs to
     * @param printedPoints victory points printed on the card
     * @param characterType type of character represented by the card
     * @param minPlayers    minimum number of players for which the card is included
     * @param hasFoodSymbol whether the card carries the food symbol (Gatherer/Hunter)
     * @param stars         number of stars on the card (Shaman)
     * @param discount      discount granted by the card (Builder)
     * @param inventionIcon icon associated with an Inventor card
     */
    public CharacterCardState(String cardId,
                              Era era,
                              int printedPoints,
                              CharacterType characterType,
                              int minPlayers,
                              Boolean hasFoodSymbol,
                              Integer stars,
                              Integer discount,
                              InventionIcon inventionIcon) {
        super(cardId, era, printedPoints);
        this.characterType = characterType;
        this.minPlayers = minPlayers;
        this.hasFoodSymbol = hasFoodSymbol;
        this.stars = stars;
        this.discount = discount;
        this.inventionIcon = inventionIcon;
    }

    /** @return the type of character represented by this card */
    public CharacterType getCharacterType() {
        return characterType;
    }

    /** @return the minimum number of players for which the card is in play */
    public int getMinPlayers() {
        return minPlayers;
    }

    /** @return whether the card displays the food symbol, or {@code null} if not applicable */
    public Boolean getHasFoodSymbol() {
        return hasFoodSymbol;
    }

    /** @return the number of stars on the card, or {@code null} if not applicable */
    public Integer getStars() {
        return stars;
    }

    /** @return the discount granted by the card, or {@code null} if not applicable */
    public Integer getDiscount() {
        return discount;
    }

    /** @return the invention icon on the card, or {@code null} if not applicable */
    public InventionIcon getInventionIcon() {
        return inventionIcon;
    }

    /** {@inheritDoc} */
    @Override
    public CardKind getCardKind() {
        return CardKind.CHARACTER;
    }

}
