package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.player.Player;

public class ExtraDrawEffect implements BuildingEffect{

    @Override
    public void onBuildingAdded(Game game, Player player){
        game.setPendingExtraDrawPlayerId(player.getId());
    }
}
