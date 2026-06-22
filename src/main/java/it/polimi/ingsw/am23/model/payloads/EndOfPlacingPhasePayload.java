package it.polimi.ingsw.am23.model.payloads;

import java.io.Serializable;
import java.util.List;

/**
 * Notification dispatched when all players completed the placing phase.
 *
 * @param playerOrderOnOfferTrack ordered list of player ids as they appear on the offer track
 * @param firstPlayerId           id of the first player due to resolve their offer
 * @param skipAllowed             whether the first player is allowed to skip the draw
 */
public record EndOfPlacingPhasePayload(List<String> playerOrderOnOfferTrack,
                                       String firstPlayerId, boolean skipAllowed) implements Serializable {
}
