package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.player.Player;

public class SustenanceDiscountPerTypeEffect implements BuildingEffect{

    private final CharacterType characterType;
    private final int discount;

    public SustenanceDiscountPerTypeEffect(CharacterType characterType, int discount){
        this.characterType = characterType;
        this.discount = discount;
    }

    @Override
    public int modifySustenanceCost(Game game, Player player, int currentCost){
        int count = player.getTribe().count(characterType);
        return currentCost - (count*discount);
    }
}
