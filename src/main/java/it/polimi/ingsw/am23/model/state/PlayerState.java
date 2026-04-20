package it.polimi.ingsw.am23.model.state;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public final class PlayerState implements Serializable {
    private final String playerId;
    private final String nickname;
    private final int food;
    private final int prestigePoints;
    private final String totemColor;
    private final List<CardState> characters;
    private final List<CardState> buildings;

    public PlayerState(String playerId,
                       String nickname,
                       int food,
                       int prestigePoints,
                       String totemColor,
                       List<CardState> characters,
                       List<CardState> buildings) {
        this.playerId = playerId;
        this.nickname = Objects.requireNonNull(nickname, "nickname is null");
        this.food = food;
        this.prestigePoints = prestigePoints;
        this.totemColor = totemColor;
        this.characters = List.copyOf(Objects.requireNonNull(characters, "characters is null"));
        this.buildings = List.copyOf(Objects.requireNonNull(buildings, "buildings is null"));
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getNickname() {
        return nickname;
    }

    public int getFood() {
        return food;
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }

    public String getTotemColor() {
        return totemColor;
    }

    public List<CardState> getCharacters() {
        return characters;
    }

    public List<CardState> getBuildings() {
        return buildings;
    }
}
