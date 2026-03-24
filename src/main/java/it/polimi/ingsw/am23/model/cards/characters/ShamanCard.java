package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;


public class ShamanCard extends CharacterCard{

    private final int stars;

    public ShamanCard(String id, Era era, int points, int stars){
        super(id, era, points, CharacterType.SHAMAN);
        this.stars = stars;
    }

    public int getStars() {
        return stars;
    }
}
