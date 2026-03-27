package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.HuntingEffectData;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.player.Player;

public class HuntingRewardPerHunterEffect implements BuildingEffect{

    @Override
    public void applyHunting(Game game, Player player, HuntingEffectData data){
        int hunters = player.getTribe().count(CharacterType.HUNTER);
        data.addExtraFood(hunters);
        data.addExtraPoints(hunters);
    } //TODO: controllare che ci sia un solo building che dà uno per hunter
}
