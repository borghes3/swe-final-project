package it.polimi.ingsw.am23.model.payloads;

import java.io.Serializable;

/**
 * Notification sent to ask the entitled player to perform their extra draw.
 *
 * @param pendingPlayerId id of the player who must perform the extra draw
 */
public record ExtraDrawRequestPayload(String pendingPlayerId) implements Serializable {
}
