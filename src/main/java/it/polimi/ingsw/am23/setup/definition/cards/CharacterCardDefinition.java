package it.polimi.ingsw.am23.setup.definition.cards;

import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.InventionIcon;

public final class CharacterCardDefinition extends CardDefinition {
    private CharacterType characterType;
    private int minPlayers;
    private Boolean hasFoodSymbol;
    private Integer stars;
    private Integer discount;
    private InventionIcon icon;

    public CharacterCardDefinition() {
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

    public InventionIcon getIcon() {
        return icon;
    }
}