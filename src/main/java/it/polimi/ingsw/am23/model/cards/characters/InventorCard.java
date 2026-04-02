package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.InventionIcon;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.CharacterCardState;

import java.util.Objects;

public class InventorCard extends CharacterCard{

    private final InventionIcon icon;

    public InventorCard(String id, Era era, int points, InventionIcon icon, int minPlayers){
        super(id, era, points, CharacterType.INVENTOR, minPlayers);
        this.icon = Objects.requireNonNull(icon, "icon cannot be null");
    }

    public InventionIcon getIcon() {
        return icon;
    }

    @Override
    public void onAddedToTribe(Game game, Player player){
        player.getTribe().incrementInventorIconCount(getIcon());
    }

    @Override
    public CardState toState(){
        return new CharacterCardState(
                getId(),
                getEra(),
                getPoints(),
                getCharacterType(),
                getMinPlayers(),
                null,
                null,
                null,
                icon
        );
    }
}
