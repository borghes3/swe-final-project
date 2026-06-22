package it.polimi.ingsw.am23.model.payloads;

import java.io.Serializable;

/**
 * Notification sent when a player places their totem on an offer tile.
 *
 * @param playerId      id of the player who placed the totem
 * @param offerTileChar letter identifying the offer tile where the totem landed
 * @param nextPlayerId  id of the next player due to act, or {@code null} when none
 */
public record TotemPlacedPayload(
        String playerId,
        char offerTileChar,
        String nextPlayerId
) implements Serializable {
}
