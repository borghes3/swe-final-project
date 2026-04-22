package it.polimi.ingsw.am23.model.state;

import java.io.Serializable;

public record ScoreEntry(String playerId, int foodPoints, int prestigePoints) implements Serializable {}
// sostituisce scoreResult, porta solo i dati (e non direttamente il player che nonè Serializable)
