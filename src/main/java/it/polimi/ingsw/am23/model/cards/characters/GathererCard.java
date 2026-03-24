package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;

public class GathererCard extends CharacterCard {
    public GathererCard(String id, Era era, int points) {
        super(id, era, points, CharacterType.GATHERER);
    }
}
