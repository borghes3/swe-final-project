package it.polimi.ingsw.am23.setup.creator.cards;

import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.cards.characters.ShamanCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.setup.definition.cards.CharacterCardDefinition;

public class ShamanCardCreator implements CharacterCardCreator {

    @Override
    public CharacterType supportedType() {
        return CharacterType.SHAMAN;
    }

    @Override
    public CharacterCard create(CharacterCardDefinition definition) {
        Integer stars = definition.getStars();
        if (stars == null) {
            throw new IllegalArgumentException("Missing stars for shaman card: " + definition.getId());
        }

        return new ShamanCard(
                definition.getId(),
                definition.getEra(),
                definition.getPoints(),
                stars,
                definition.getMinPlayers()
        );
    }
}