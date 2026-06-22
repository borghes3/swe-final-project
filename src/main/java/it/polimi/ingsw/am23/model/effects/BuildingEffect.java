package it.polimi.ingsw.am23.model.effects;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.player.Player;

/**
 * Persistent effect attached to a building card. Every method has a no-op
 * default implementation, so concrete effects only override the hooks they
 * actually need to react to.
 */
public interface BuildingEffect {

    /**
     * Lets the effect modify the food delta granted by the turn order slot
     * the player landed on.
     *
     * @param game        current game instance
     * @param player      the affected player
     * @param currentFood food delta computed so far
     * @return the modified food delta
     */
    default int modifyTurnOrderFood(Game game, Player player, int currentFood) {
        return currentFood;
    }

    /**
     * Hook invoked whenever a card is taken by {@code player}. Useful for
     * effects that track set / pair completion (e.g. inventor pairs).
     *
     * @param game   current game instance
     * @param player the player who took the card
     * @param card   the card that has just been taken
     */
    default void onCardTaken(Game game, Player player, Card card) {

    }

    /**
     * Lets the effect modify the sustenance cost computed by the sustenance
     * event.
     *
     * @param game        current game instance
     * @param player      the affected player
     * @param currentCost cost computed so far
     * @return the modified cost
     */
    default int modifySustenanceCost(Game game, Player player, int currentCost) {
        return currentCost;
    }

    /**
     * Hook applied during the resolution of a hunting event to add bonus
     * food and/or prestige points.
     *
     * @param game   current game instance
     * @param player the affected player
     * @param data   accumulator of the bonuses produced by all effects
     */
    default void applyHunting(Game game, Player player, HuntingEffectData data) {

    }

    /**
     * Hook applied during the resolution of a cave paintings event to add
     * bonus food.
     *
     * @param game   current game instance
     * @param player the affected player
     * @param data   accumulator of the bonuses produced by all effects
     */
    default void applyCavePaintings(Game game, Player player, CavePaintingsEffectData data) {

    }

    /**
     * Hook applied during the resolution of a shaman ritual event to add
     * bonus stars or alter the win/loss behavior.
     *
     * @param game   current game instance
     * @param player the affected player
     * @param data   accumulator of the bonuses produced by all effects
     */
    default void applyShamanRitual(Game game, Player player, ShamanRitualEffectData data) {
    }

    /**
     * Hook invoked after a player completes all their draw actions, used by
     * effects that trigger an additional draw.
     *
     */
    default void onAfterAllActions() {
    }

    /**
     * Bonus points contributed by this effect at the end of the game.
     *
     * @param game   current game instance
     * @param player the affected player
     * @return the end of game points contributed by this effect
     */
    default int getEndGamePoints(Game game, Player player) {
        return 0;
    }

    /**
     * Hook invoked right after this effect's owning building is purchased.
     *
     * @param game   current game instance
     * @param player the player who just acquired the building
     */
    default void onBuildingAdded(Game game, Player player) {
    }
}
