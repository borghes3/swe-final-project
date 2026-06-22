package it.polimi.ingsw.am23.model.payloads;

import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.state.CardState;

import java.io.Serializable;

/**
 * Notification sent when a player consumed an extra draw opportunity granted
 * by a building effect.
 *
 * @param playerId     id of the player who took the extra card
 * @param cardId       id of the card taken
 * @param takenCard    full state of the card taken
 * @param building     {@code true} if the extra card is a building, {@code false} for a tribe card
 * @param absoluteFood player's total food after the extra draw
 * @param newPhase     game phase after the extra draw
 * @param skipAllowed  whether subsequent skips are still allowed
 */
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
