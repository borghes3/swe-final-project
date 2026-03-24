package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.InventionIcon;

import java.util.Objects;

public class InventorCard extends CharacterCard{

    private final InventionIcon icon;

    public InventorCard(String id, Era era, int points, InventionIcon icon){
        super(id, era, points, CharacterType.INVENTOR);
        this.icon = Objects.requireNonNull(icon, "icon cannot be null");
    }

    public InventionIcon getIcon() {
        return icon;
    }
}
