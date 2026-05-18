package it.polimi.ingsw.am23.model.payloads;

import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.OfferTileState;
import it.polimi.ingsw.am23.model.state.TurnOrderSlotState;

import java.io.Serializable;
import java.util.List;

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