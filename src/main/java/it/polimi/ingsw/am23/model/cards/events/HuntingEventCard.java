package it.polimi.ingsw.am23.model.cards.events;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.EventCard;
import it.polimi.ingsw.am23.model.effects.HuntingEffectData;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;

import java.util.List;

public class HuntingEventCard extends EventCard {

    public HuntingEventCard(String id, Era era, int points, boolean isFinal){
        super(id, era, points, isFinal);
    }

    @Override
    public void resolve(Game game){
        List<Player> players = game.getPlayers();

        for(Player player : players){
            int baseFood = calculateBaseFood(player);
            player.addFood(baseFood);

            HuntingEffectData data = new HuntingEffectData();
            for(BuildingCard building : player.getTribe().getBuildings()){
                building.getEffect().applyHunting(game, player, data);
            }
            player.addFood(data.getExtraFood());
            player.addPrestigePoints(data.getExtraPoints());
        }

    }

    private int calculateBaseFood(Player player){
        return player.getTribe().count(CharacterType.HUNTER);
    }
}
