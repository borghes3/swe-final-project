package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.CharacterCardState;

public class HunterCard extends CharacterCard{

    private final boolean hasFoodSymbol;

    public HunterCard(String id, Era era, int points, boolean hasFoodSymbol, int minPlayers) {
        super(id, era, points, CharacterType.HUNTER,  minPlayers);
        this.hasFoodSymbol = hasFoodSymbol;
    }

    @Override
    public void onAddedToTribe(Game game, Player player){ //pesca 1 di cibo per ogni hunter in tribe se il cacciatore pescato ha l'icona
        if(hasFoodSymbol){
            int huntersInTribe = player.getTribe().count(CharacterType.HUNTER);
            player.applyFoodDelta(huntersInTribe);
        }
    }

    @Override
    public CardState toState(){
        return new CharacterCardState(
                getId(),
                getEra(),
                getPoints(),
                getCharacterType(),
                getMinPlayers(),
                hasFoodSymbol,
                null,
                null,
                null
        );
    }
}
