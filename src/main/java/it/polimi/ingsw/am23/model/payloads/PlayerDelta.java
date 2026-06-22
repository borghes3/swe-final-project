package it.polimi.ingsw.am23.model.payloads;

import java.io.Serializable;

/**
 * Per-player resource delta produced by a game event, paired with the
 * absolute values reached after the event has been applied.
 *
 * @param playerId         id of the affected player
 * @param foodDelta        signed change to the player's food reserve
 * @param prestigeDelta    signed change to the player's prestige points
 * @param absoluteFood     player's food reserve after the event
 * @param absolutePrestige player's prestige points after the event
 */
public record PlayerDelta(String playerId, int foodDelta, int prestigeDelta, int absoluteFood, int absolutePrestige) implements Serializable {}
