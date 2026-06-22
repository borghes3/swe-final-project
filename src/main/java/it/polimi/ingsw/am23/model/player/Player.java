package it.polimi.ingsw.am23.model.player;

import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.state.PlayerState;

import java.util.Objects;

/**
 * Represents a player participating in the match. Owns the player's
 * resources (food, prestige points), the {@link Tribe} of collected cards
 * and the {@link Totem} used to mark board areas.
 */
public class Player {

    private final String id;
    private final String nickname;
    private int food;
    private int prestigePoints;
    private final Tribe tribe;
    private final Totem totem;

    /**
     * Builds a new player.
     *
     * @param id             unique identifier of the player
     * @param nickname       display nickname
     * @param food           starting food reserve (must be non negative)
     * @param prestigePoints starting prestige points
     * @param totem          the totem assigned to the player
     * @throws IllegalArgumentException if {@code food} is negative
     */
    public Player(String id, String nickname, int food, int prestigePoints, Totem totem) {
        this.id = Objects.requireNonNull(id);
        this.nickname = Objects.requireNonNull(nickname);

        if (food < 0) {
            throw new IllegalArgumentException("initialFood or prestigePoints cannot be negative");
        }

        this.food = food;
        this.prestigePoints = prestigePoints;
        this.tribe = new Tribe();
        this.totem = totem;
    }

    /** @return the unique identifier of this player */
    public String getId() {
        return id;
    }

    /** @return the player's current food reserve */
    public int getFood() {
        return food;
    }

    /** @return the player's current prestige points */
    public int getPrestigePoints() {
        return prestigePoints;
    }

    /** @return the tribe owned by this player */
    public Tribe getTribe() {
        return tribe;
    }

    /**
     * Applies a signed delta to the player's food reserve.
     *
     * @param amount delta to apply (negative = spend)
     * @throws IllegalArgumentException if the resulting food would be negative
     */
    public void applyFoodDelta(int amount) {
        if (amount < 0 && -amount > food) {
            System.out.println("ERROR applyFoodDelta: food=" + food + " amount=" + amount + " would go negative!");
            throw new IllegalArgumentException("Not enough food");
        }
        food += amount;
    }

    /**
     * Adds prestige points to the player's total.
     *
     * @param amount positive number of points to add
     * @throws IllegalArgumentException if {@code amount} is negative
     */
    public void addPrestigePoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
        prestigePoints += amount;
    }

    /**
     * Checks whether the player has enough food to cover the supplied
     * absolute amount.
     *
     * @param amount cost to check (sign is ignored)
     * @return {@code true} if the player can pay
     */
    public boolean canAfford(int amount) {
        return food >= Math.abs(amount);
    }

    /**
     * Subtracts the supplied absolute amount from the player's prestige
     * points; values can go below zero.
     *
     * @param amount cost to pay (sign is ignored)
     */
    public void spendPrestigePoints(int amount) {
        prestigePoints -= Math.abs(amount);
    }

    /** @return the serializable snapshot of this player */
    public PlayerState getState() {
        return new PlayerState(
                id,
                nickname,
                food,
                prestigePoints,
                totem != null ? totem.getColor() : null,
                tribe.getCharacters().stream().map(CharacterCard::toState).toList(),
                tribe.getBuildings().stream().map(BuildingCard::toState).toList()
        );
    }
}
