package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.model.payloads.*;

// --------------------------------------------
// INTERFACCIA OSSERVATA DALLA VIRTUAL VIEW
// --------------------------------------------
//      [MODEL](*) ------> [VIRTUAL VIEW]
//              ^
//              |
//      Questa interfaccia sta qui
// --------------------------------------------

@Deprecated
public interface ModelObserverPayloads {

    void onGameStarted(GameStartedPayload payload); // manda tutto gameState !

    //void onGameStateChanged(GameState gameState);

    void onTotemPlaced(TotemPlacedPayload payload);

    void onEndOfPlacingPhase(EndOfPlacingPhasePayload payload);

    void onCardsTaken(CardsTakenPayload payload);

    void onExtraDrawRequest(ExtraDrawRequestPayload payload);

    void onExtraCardTaken(ExtraCardTakenPayload payload);

    // Da qui si deduce: END OF ROUND
    void onEventResolved(EventResolvedPayload payload);

    void onEraProgression(EraProgressionPayload payload);

    void onMarketRefreshed(MarketRefresherPayload payload);

    void onGameOver();

    void onScoreboardAvailable(ScoreBoardPayload payload);
    // void onScores(List<ScoreResult> scoreBoard);
}
