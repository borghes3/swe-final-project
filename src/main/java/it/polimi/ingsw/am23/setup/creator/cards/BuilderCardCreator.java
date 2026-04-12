package it.polimi.ingsw.am23.setup.creator.cards;

import it.polimi.ingsw.am23.model.cards.characters.BuilderCard;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.setup.definition.cards.CharacterCardDefinition;

public class BuilderCardCreator implements CharacterCardCreator {

    @Override
    public CharacterType supportedType() {
        return CharacterType.BUILDER;
    }

    @Override
    public CharacterCard create(CharacterCardDefinition definition) {
        Integer discount = definition.getDiscount();
        if (discount == null) {
            throw new IllegalArgumentException("Missing discount for builder card: " + definition.getId());
        }

        return new BuilderCard(
                definition.getId(),
                definition.getEra(),
                definition.getPoints(),
                definition.getMinPlayers(),
                discount
        );
    }
}