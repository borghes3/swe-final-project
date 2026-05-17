package it.polimi.ingsw.am23.model.payloads;

import java.io.Serializable;

public record PlayerDelta(String playerId, int foodDelta, int prestigeDelta, int absoluteFood, int absolutePrestige) implements Serializable {}
