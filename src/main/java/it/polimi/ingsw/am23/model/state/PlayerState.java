package it.polimi.ingsw.am23.model.state;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of a player, including their resources, totem color
 * and the cards they currently own.
 */
public final class PlayerState implements Serializable {
    private final String playerId;
    private final String nickname;
    private final int food;
    private final int prestigePoints;
    private final String totemColor;
    private final List<CardState> characters;
    private final List<CardState> buildings;

    /**
     * Builds a new player snapshot.
     *
     * @param playerId       unique identifier of the player
     * @param nickname       display nickname of the player
     * @param food           current food reserve
     * @param prestigePoints current prestige points
     * @param totemColor     color of the player's totem
     * @param characters     snapshot of the owned character cards
     * @param buildings      snapshot of the owned building cards
     */
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

    /** @return the unique identifier of the player */
    public String getPlayerId() {
        return playerId;
    }

    /** @return the display nickname of the player */
    public String getNickname() {
        return nickname;
    }

    /** @return the player's current food reserve */
    public int getFood() {
        return food;
    }

    /** @return the player's current prestige points */
    public int getPrestigePoints() {
        return prestigePoints;
    }

    /** @return the color of the player's totem */
    public String getTotemColor() {
        return totemColor;
    }

    /** @return snapshot of the character cards owned by the player */
    public List<CardState> getCharacters() {
        return characters;
    }

    /** @return snapshot of the building cards owned by the player */
    public List<CardState> getBuildings() {
        return buildings;
    }
}
