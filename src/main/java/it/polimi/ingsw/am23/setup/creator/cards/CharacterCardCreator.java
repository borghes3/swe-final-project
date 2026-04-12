package it.polimi.ingsw.am23.setup.creator.cards;

import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.setup.definition.cards.CharacterCardDefinition;

public interface CharacterCardCreator {
    CharacterType supportedType();
    CharacterCard create(CharacterCardDefinition definition);
}
