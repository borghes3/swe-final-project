package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.state.GameState;

/**
 * Interface exposed by the model to the controller layer.
 * Defines the set of actions the controller can issue (placing totems,
 * drawing cards, resolving events, ...) and the observer registration
 * helpers used to subscribe to model notifications.
 */
public interface GameModel {

    /**
     * Transitions from setup to the first placing phase.
     */
    void startGame();

    /**
     * Places the totem of the supplied player on the offer tile identified
     * by {@code offerTileChar}.
     *
     * @param playerId      id of the player placing the totem
     * @param offerTileChar letter of the offer tile
     * @return the outcome of the action
     */
    ActionResult placeTotem(String playerId, char offerTileChar);

    /**
     * Takes a single card from the card market on behalf of the player.
     *
     * @param playerId           id of the acting player
     * @param selectedSingleCard the card selection
     * @return the outcome of the action
     */
    ActionResult takeSingleCard(String playerId, SelectedSingleCard selectedSingleCard);

    /**
     * Performs an extra draw on behalf of the player owning the extra draw
     * entitlement.
     *
     * @param playerId              id of the acting player
     * @param selectedCardExtraDraw the extra draw selection
     * @return the outcome of the action
     */
    ActionResult takeExtraCard(String playerId, SelectedCardExtraDraw selectedCardExtraDraw);

    /**
     * Skips the draw turn of the supplied player.
     *
     * @param playerId id of the player skipping the turn
     * @return the outcome of the action
     */
    ActionResult skipTurn(String playerId);

    /**
     * Triggers the resolution of the event cards present on the bottom row
     * (and on the top row during the final round).
     *
     * @return the outcome of the action
     */
    ActionResult resolveEvents();

    /**
     * Computes the final scoreboard at the end of the match.
     *
     * @return the outcome of the action
     */
    ActionResult calculateScores();

    /**
     * @return the latest snapshot of the game state
     */
    GameState getGameState();

    /**
     * @return the current game phase
     */
    GamePhase getGamePhase();

    /**
     * Registers an observer interested in the model notifications.
     *
     * @param observer observer to subscribe
     */
    void addObserver(ModelObserver observer);

    /**
     * Unregisters a previously subscribed observer.
     *
     * @param observer observer to unsubscribe
     */
    void removeObserver(ModelObserver observer);
}
