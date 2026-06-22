package it.polimi.ingsw.am23.model.payloads;

import java.io.Serializable;
import java.util.Map;

/**
 * Final score breakdown for a single player.
 *
 * @param playerId            id of the player
 * @param nickname            display nickname of the player
 * @param totalPrestigePoints total prestige points at the end of the match
 * @param foodPoints          points coming from the leftover food reserve
 * @param breakdown           optional category to points map for detailed reporting
 */
public record PlayerScore(String playerId, String nickname, int totalPrestigePoints, int foodPoints,
                          Map<String, Integer> breakdown) implements Serializable {
}
