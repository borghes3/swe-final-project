package it.polimi.ingsw.am23.model.payloads;

import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.OfferTileState;
import it.polimi.ingsw.am23.model.state.TurnOrderSlotState;

import java.io.Serializable;
import java.util.List;

/**
 * Notification sent during the end-of-round cleanup describing how the card
 * market and the surrounding board areas have been refreshed.
 *
 * @param discardedCardIds   ids of the cards discarded from the bottom row
 * @param movedBottomCardIds ids of the cards moved from the top to the bottom row
 * @param newUpperRowCardIds ids of the freshly drawn cards in the upper row
 * @param newUpperRowCards   full state of the freshly drawn upper row cards
 * @param offerTiles         state of all offer tiles after the refresh
 * @param turnOrderSlots     state of all turn order slots after the refresh
 * @param newRound           round number after the refresh
 * @param newPhase           game phase after the refresh
 * @param nextPlayerId       id of the next player due to act (may be {@code null})
 * @param skipAllowed        whether the next player is allowed to skip the draw
 */
public record MarketRefresherPayload(
        List<String> discardedCardIds,
        List<String> movedBottomCardIds,
        List<String> newUpperRowCardIds,
        List<CardState> newUpperRowCards,
        List<OfferTileState> offerTiles,
        List<TurnOrderSlotState> turnOrderSlots,
        int newRound,
        GamePhase newPhase,
        String nextPlayerId,
        boolean skipAllowed
) implements Serializable {
}
