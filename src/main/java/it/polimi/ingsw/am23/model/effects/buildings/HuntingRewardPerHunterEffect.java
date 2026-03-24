package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.HuntingEffectData;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.player.Player;

public class HuntingRewardPerHunterEffect implements BuildingEffect{

    private final int foodPerHunter;
    private final int pointsPerHunter;

    public HuntingRewardPerHunterEffect(int foodPerHunter, int pointsPerHunter){
        this.foodPerHunter = foodPerHunter;
        this.pointsPerHunter = pointsPerHunter;
    }

    @Override
    public void applyHunting(Game game, Player player, HuntingEffectData data){
        int hunters = player.getTribe().count(CharacterType.HUNTER);
        data.addExtraFood(foodPerHunter*hunters);
        data.addExtraPoints(pointsPerHunter*hunters);
    }
}
