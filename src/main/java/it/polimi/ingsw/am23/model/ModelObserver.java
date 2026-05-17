package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.model.payloads.*;

public interface ModelObserver {
    void onGameStarted(GameStartedPayload payload); // manda snapshot completo

    void onTotemPlaced(TotemPlacedPayload payload);

    void onEndOfPlacingPhase(EndOfPlacingPhasePayload payload);

    void onCardsTaken(CardsTakenPayload payload);

    void onExtraDrawRequest(ExtraDrawRequestPayload payload);

    void onExtraCardTaken(ExtraCardTakenPayload payload);

    void onEventResolved(EventResolvedPayload payload);

    void onMarketRefreshed(MarketRefresherPayload payload);

    void onEraProgression(EraProgressionPayload payload);

    void onGameOver();

    void onScoreboardAvailable(ScoreBoardPayload payload);

}
