package it.polimi.ingsw.am23.model.payloads;

import java.io.Serializable;

public record ExtraDrawRequestPayload(String pendingPlayerId) implements Serializable {
}
