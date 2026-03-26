package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;

public class ArtistCard extends CharacterCard {
    public ArtistCard(String id, Era era, int points, int minPlayers) {
        super(id, era, points, CharacterType.ARTIST, minPlayers);
    }
}
