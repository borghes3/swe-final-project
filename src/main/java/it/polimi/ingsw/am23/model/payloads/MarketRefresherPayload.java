package it.polimi.ingsw.am23.model.payloads;

import it.polimi.ingsw.am23.model.state.CardState;

import java.io.Serializable;
import java.util.List;

public record MarketRefresherPayload(List<String> discardedCardIds, List<String> movedBottomCardIds, List<String> newUpperRowCardIds, List<CardState> newUpperRowCards) implements Serializable {
}
