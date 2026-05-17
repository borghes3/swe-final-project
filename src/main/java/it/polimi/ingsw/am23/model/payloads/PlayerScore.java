package it.polimi.ingsw.am23.model.payloads;

import java.io.Serializable;
import java.util.Map;

public record PlayerScore(String playerId, String nickname, int totalPrestigePoints, int foodPoints, Map<String, Integer> breakdown) implements Serializable {}
