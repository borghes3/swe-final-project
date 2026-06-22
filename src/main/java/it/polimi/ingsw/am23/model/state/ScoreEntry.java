package it.polimi.ingsw.am23.model.state;

import java.io.Serializable;

/**
 * Serializable per player score entry. Used in place of the in-memory
 * {@code ScoreResult} for end-of-game payloads, since it does not carry the
 * non serializable {@code Player} reference.
 *
 * @param playerId       id of the player the score refers to
 * @param foodPoints     points coming from the leftover food reserve
 * @param prestigePoints total prestige points scored
 */
public record ScoreEntry(String playerId, int foodPoints, int prestigePoints) implements Serializable {}
