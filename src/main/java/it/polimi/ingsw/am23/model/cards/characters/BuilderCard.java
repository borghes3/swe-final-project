package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.CharacterCardState;

public class BuilderCard extends CharacterCard{

    private final int discount;

    public BuilderCard(String id, Era era, int points, int discount, int minPlayers) {
        super(id, era, points, CharacterType.BUILDER, minPlayers);
        this.discount = discount;
    }

    @Override
    public int getDiscount() {
        return discount;
    }

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
