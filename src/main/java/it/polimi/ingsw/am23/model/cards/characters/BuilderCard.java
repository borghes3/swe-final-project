package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;

public class BuilderCard extends CharacterCard{

    private final int discount;

    public BuilderCard(String id, Era era, int points, int discount) {
        super(id, era, points, CharacterType.BUILDER);
        this.discount = discount;
    }

    public int getDiscount() {
        return discount;
    }
}
