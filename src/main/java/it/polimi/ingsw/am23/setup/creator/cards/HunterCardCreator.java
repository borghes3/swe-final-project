package it.polimi.ingsw.am23.setup.creator.cards;

import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.cards.characters.HunterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.setup.definition.cards.CharacterCardDefinition;

public class HunterCardCreator implements CharacterCardCreator {

    @Override
    public CharacterType supportedType() {
        return CharacterType.HUNTER;
    }

    @Override
    public CharacterCard create(CharacterCardDefinition definition) {
        Boolean hasFoodSymbol = definition.getHasFoodSymbol();
        if (hasFoodSymbol == null) {
            throw new IllegalArgumentException("Missing hasFoodSymbol for hunter card: " + definition.getId());
        }

        return new HunterCard(
                definition.getId(),
                definition.getEra(),
                definition.getPoints(),
                hasFoodSymbol,
                definition.getMinPlayers()
        );
    }
}
