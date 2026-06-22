package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.player.Player;

/**
 * End-of-game building effect that grants extra prestige points equal to
 * the sum of the printed points of every character card owned by the
 * player. This stacks on top of the normal character point scoring.
 */
public class DoubleBuilderEndGameEffect implements BuildingEffect{

    /** {@inheritDoc} */
    @Override
    public int getEndGamePoints(Game game, Player player){
        int bonus = 0;

        for(CharacterCard card : player.getTribe().getCharacters()){
                bonus += card.getPoints();
        }
        return bonus;
    }

}
