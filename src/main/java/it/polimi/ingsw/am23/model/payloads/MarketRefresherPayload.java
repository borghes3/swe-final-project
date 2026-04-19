package it.polimi.ingsw.am23.model.payloads;

import java.util.List;

public record MarketRefresherPayload(List<String> discardedCardIds, List<String> movedBottomCardIds, List<String> newUpperRowCardIds) {
}
