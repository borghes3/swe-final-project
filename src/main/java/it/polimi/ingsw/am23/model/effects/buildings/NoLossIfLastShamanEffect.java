package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.ShamanRitualEffectData;
import it.polimi.ingsw.am23.model.player.Player;

public class NoLossIfLastShamanEffect implements BuildingEffect{

    @Override
    public void applyShamanRitual(Game game, Player player, ShamanRitualEffectData data){
        data.setIgnoreLoss(true);
    }
}
