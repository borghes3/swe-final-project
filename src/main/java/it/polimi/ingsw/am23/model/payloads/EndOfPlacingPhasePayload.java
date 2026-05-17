package it.polimi.ingsw.am23.model.payloads;

import java.io.Serializable;
import java.util.List;

public record EndOfPlacingPhasePayload(List<String> playerOrderOnOfferTrack,
                                       String firstPlayerId, boolean skipAllowed) implements Serializable {
}
