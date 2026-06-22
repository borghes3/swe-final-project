package it.polimi.ingsw.am23.model.state;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of a player, including their resources, totem color
 * and the cards they currently own.
 *
 * @param playerId       unique identifier of the player
 * @param nickname       display nickname of the player
 * @param food           current food reserve
 * @param prestigePoints current prestige points
 * @param totemColor     color of the player's totem
 * @param characters     snapshot of the owned character cards
 * @param buildings      snapshot of the owned building cards
 */
public record PlayerState(String playerId, String nickname, int food, int prestigePoints, String totemColor,
                          List<CardState> characters, List<CardState> buildings) implements Serializable {
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

    /**
     * Returns the unique id of this player.
     *
     * @return the unique identifier of the player
     */
    @Override
    public String playerId() {
        return playerId;
    }

    /**
     * Returns the nickname shown in the UI for this player.
     *
     * @return the display nickname of the player
     */
    @Override
    public String nickname() {
        return nickname;
    }

    /**
     * Returns how much food the player currently holds.
     *
     * @return the player's current food reserve
     */
    @Override
    public int food() {
        return food;
    }

    /**
     * Returns how many prestige points the player has earned so far.
     *
     * @return the player's current prestige points
     */
    @Override
    public int prestigePoints() {
        return prestigePoints;
    }

    /**
     * Returns the color of the totem used by this player on the board.
     *
     * @return the color of the player's totem
     */
    @Override
    public String totemColor() {
        return totemColor;
    }

    /**
     * Returns the character cards currently owned by this player.
     *
     * @return snapshot of the character cards owned by the player
     */
    @Override
    public List<CardState> characters() {
        return characters;
    }

    /**
     * Returns the building cards currently owned by this player.
     *
     * @return snapshot of the building cards owned by the player
     */
    @Override
    public List<CardState> buildings() {
        return buildings;
    }
}
