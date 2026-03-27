package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.cards.characters.BuilderCard;
import it.polimi.ingsw.am23.model.cards.characters.InventorCard;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.player.Player;

public class DoubleBuilderEndGameEffect implements BuildingEffect{

    @Override
    public int getEndGamePoints(Game game, Player player){
        int bonus = 0;

        for(CharacterCard card : player.getTribe().getCharacters()){
            if(card instanceof BuilderCard){ //TODO: provare a togliere instanceof
                bonus += card.getPoints();
            }
        }
        return bonus;
    }

}

//questo presuppone che i punti vengano già una volta contati normalmente
