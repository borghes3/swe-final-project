package it.polimi.ingsw.am23.model.payloads;

import java.util.Map;

public record PlayerScore(String playerId, int totalPrestigePoints, Map<String, Integer> breakdown) {}
