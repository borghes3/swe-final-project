package it.polimi.ingsw.am23.setup.creator.cards;

import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.cards.characters.InventorCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.InventionIcon;
import it.polimi.ingsw.am23.setup.definition.cards.CharacterCardDefinition;

public class InventorCardCreator implements CharacterCardCreator {

    @Override
    public CharacterType supportedType() {
        return CharacterType.INVENTOR;
    }

    @Override
    public CharacterCard create(CharacterCardDefinition definition) {
        InventionIcon icon = definition.getIcon();
        if (icon == null) {
            throw new IllegalArgumentException("Missing icon for inventor card: " + definition.getId());
        }

        return new InventorCard(
                definition.getId(),
                definition.getEra(),
                definition.getPoints(),
                icon,
                definition.getMinPlayers()
        );
    }
}
