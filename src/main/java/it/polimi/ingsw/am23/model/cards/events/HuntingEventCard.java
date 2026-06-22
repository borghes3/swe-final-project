package it.polimi.ingsw.am23.model.cards.events;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.EventCard;
import it.polimi.ingsw.am23.model.effects.HuntingEffectData;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;

import java.util.List;

/**
 * Event card implementing the Hunting scoring rule.
 * Each player gains one food per Hunter owned plus
 * {@code pointsPerHunter} prestige points per Hunter; building effects can
 * extend both the food and the prestige reward.
 */
public class HuntingEventCard extends EventCard {

    private final int pointsPerHunter;

    /**
     * Builds a new Hunting event card.
     *
     * @param id              unique identifier of the card
     * @param era             era the card belongs to
     * @param points          printed victory points
     * @param isFinal         whether the card is resolved only at end of match
     * @param pointsPerHunter prestige points awarded per Hunter owned
     */
    public HuntingEventCard(String id, Era era, int points, boolean isFinal, int pointsPerHunter) {
        super(id, era, points, isFinal);
        this.pointsPerHunter = pointsPerHunter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolve(Game game) {
        List<Player> players = game.getPlayers();

        for (Player player : players) {
            int baseFood = calculateBaseFood(player);
            int basePoints = calculateBasePoints(player);
            player.applyFoodDelta(baseFood);
            player.addPrestigePoints(basePoints);

            HuntingEffectData data = new HuntingEffectData();
            for (BuildingCard building : player.getTribe().getBuildings()) {
                building.getEffect().applyHunting(game, player, data);
            }
            player.applyFoodDelta(data.getExtraFood());
            player.addPrestigePoints(data.getExtraPoints());
        }

    }

    private int calculateBaseFood(Player player) {
        return player.getTribe().count(CharacterType.HUNTER);
    }

    private int calculateBasePoints(Player player) {
        return player.getTribe().count(CharacterType.HUNTER) * pointsPerHunter;
    }
}
