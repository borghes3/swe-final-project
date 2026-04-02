package it.polimi.ingsw.am23.model.state;

import it.polimi.ingsw.am23.model.enums.CardKind;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.InventionIcon;

public final class CharacterCardState extends CardState {

    private final CharacterType characterType;
    private final int minPlayers;

    //optional attributes for the various card types
    private final Boolean hasFoodSymbol;
    private final Integer stars;
    private final Integer discount;
    private final InventionIcon inventionIcon;

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

    public CharacterType getCharacterType() {
        return characterType;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public Boolean getHasFoodSymbol() {
        return hasFoodSymbol;
    }

    public Integer getStars() {
        return stars;
    }

    public Integer getDiscount() {
        return discount;
    }

    public InventionIcon getInventionIcon() {
        return inventionIcon;
    }

    @Override
    public CardKind getCardKind() {
        return CardKind.CHARACTER;
    }

}
