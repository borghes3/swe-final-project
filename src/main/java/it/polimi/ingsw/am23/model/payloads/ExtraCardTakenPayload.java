package it.polimi.ingsw.am23.model.payloads;

import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.state.CardState;

import java.io.Serializable;

public record ExtraCardTakenPayload(
        String playerId,
        String cardId,
        CardState takenCard,
        boolean building,
        int absoluteFood,
        GamePhase newPhase,
        boolean skipAllowed
) implements Serializable {
}
