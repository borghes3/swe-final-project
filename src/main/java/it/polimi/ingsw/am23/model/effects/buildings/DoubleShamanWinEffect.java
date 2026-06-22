package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.ShamanRitualEffectData;
import it.polimi.ingsw.am23.model.player.Player;


/**
 * Shaman ritual building effect that doubles the prestige points awarded
 * to the player when they win the ritual.
 */
public class DoubleShamanWinEffect implements BuildingEffect{

    /** {@inheritDoc} */
    @Override
    public void applyShamanRitual(Game game, Player player, ShamanRitualEffectData data){
        data.setDoubleWin(true);
    }
}
