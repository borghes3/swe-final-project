package it.polimi.ingsw.am23.model.cards.events;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.EventCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;

import java.util.List;

/**
 * Event card implementing the Sustenance rule. Each player must feed their
 * characters; the cost is reduced by Gatherers and modulated by building
 * effects. Players unable to fully pay in food spend prestige points at a
 * rate scaled by the current era.
 */
public class SustenanceEventCard extends EventCard {
    /**
     * Builds a new Sustenance event card.
     *
     * @param id      unique identifier of the card
     * @param era     era the card belongs to
     * @param points  printed victory points
     * @param isFinal whether the card is resolved only at end of match
     */
    public SustenanceEventCard(String id, Era era, int points, boolean isFinal) {
        super(id, era, points, isFinal);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code 1}: sustenance is resolved before other tied events
     */
    @Override
    public int getResolutionPriority() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolve(Game game) {
        List<Player> players = game.getPlayers();

        for (Player player : players) {
            int cost = calculateBaseCost(player);

            for (BuildingCard building : player.getTribe().getBuildings()) {
                cost = building.getEffect().modifySustenanceCost(game, player, cost);
            }

            // Defensive clamp: discounts should never produce a negative cost
            cost = Math.max(cost, 0);
            if (player.getFood() >= cost) {
                player.applyFoodDelta(-cost);
            } else {
                int playerFood = player.getFood();
                player.applyFoodDelta(-playerFood);
                cost = cost - playerFood;
                player.spendPrestigePoints(cost * (game.getCurrentEra().ordinal() + 1));
            }
        }
    }

    private int calculateBaseCost(Player player) {
        return player.getTribe().getCharacters().size() - 3 * player.getTribe().count(CharacterType.GATHERER);
    }
}
