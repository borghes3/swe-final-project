package it.polimi.ingsw.am23.model.cards.events;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.EventCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;

import java.util.List;

public class SustenanceEventCard extends EventCard {
    public SustenanceEventCard(String id, Era era, int points, boolean isFinal) {
        super(id, era, points, isFinal);
    }

    @Override
    public void resolve(Game game) {
        List<Player> players = game.getPlayers();

        for (Player player : players) {
            int cost = calculateBaseCost(player);

            for (BuildingCard building : player.getTribe().getBuildings()) {
                cost = building.getEffect().modifySustenanceCost(game, player, cost);
            }

            cost = Math.max(cost, 0); //non dovrebbe servire ma non si sa mai
            if (player.getFood() >= cost) {
                player.applyFoodDelta(-cost);
            } else {
                int playerFood = player.getFood();
                player.applyFoodDelta(-playerFood);
                cost = cost - playerFood;
                player.spendPrestigePoints(cost * (game.getCurrentEra().ordinal() + 1));
            }
            // game.applyFoodCostWithPointsFallback(player, cost);
        }
    }

    private int calculateBaseCost(Player player) {
        return player.getTribe().getCharacters().size() - 3 * player.getTribe().count(CharacterType.GATHERER);
    }
}
