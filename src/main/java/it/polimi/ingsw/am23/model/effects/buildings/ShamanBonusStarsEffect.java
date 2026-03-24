package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.effects.ShamanRitualEffectData;
import it.polimi.ingsw.am23.model.player.Player;

public class ShamanBonusStarsEffect implements BuildingEffect{

    private final int bonusStars;
    public ShamanBonusStarsEffect(int bonusStars){
        this.bonusStars = bonusStars;
    }
    @Override
    public void applyShamanRitual(Game game, Player player, ShamanRitualEffectData data){
        data.setBonusStars(bonusStars);
    }
}
