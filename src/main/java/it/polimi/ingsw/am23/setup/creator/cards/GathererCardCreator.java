package it.polimi.ingsw.am23.setup.creator.cards;

import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.cards.characters.GathererCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.setup.definition.cards.CharacterCardDefinition;

public class GathererCardCreator implements CharacterCardCreator {

    @Override
    public CharacterType supportedType() {
        return CharacterType.GATHERER;
    }

    @Override
    public CharacterCard create(CharacterCardDefinition definition) {
        return new GathererCard(
                definition.getId(),
                definition.getEra(),
                definition.getPoints(),
                definition.getMinPlayers()
        );
    }
}