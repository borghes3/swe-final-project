package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.HuntingEffectData;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.player.Player;

/**
 * Building effect applied during the Hunting event: contributes one extra
 * food and one extra prestige point per Hunter owned by the player.
 */
public class HuntingRewardPerHunterEffect implements BuildingEffect{

    /** {@inheritDoc} */
    @Override
    public void applyHunting(Game game, Player player, HuntingEffectData data){
        int hunters = player.getTribe().count(CharacterType.HUNTER);
        data.addExtraFood(hunters);
        data.addExtraPoints(hunters);
    }
}
