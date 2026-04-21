package it.polimi.ingsw.am23.setup.creator.cards;

import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.cards.characters.ArtistCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.setup.definition.cards.CharacterCardDefinition;

public class ArtistCardCreator implements CharacterCardCreator {

    @Override
    public CharacterType supportedType() {
        return CharacterType.ARTIST;
    }

    @Override
    public CharacterCard create(CharacterCardDefinition definition) {
        return new ArtistCard(
                definition.getId(),
                definition.getEra(),
                definition.getPoints(),
                definition.getMinPlayers()
        );
    }
}
