package it.polimi.ingsw.am23.model.player;

import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.state.PlayerState;

import java.util.Objects;

public class Player {

    private final String id;
    private final String nickname;
    private int food;
    private int prestigePoints;
    private final Tribe tribe;
    private final Totem totem;

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

    public String getId() {
        return id;
    }

    public int getFood() {
        return food;
    }

    public int getPrestigePoints() {
        return prestigePoints;
    }

    public Tribe getTribe() {
        return tribe;
    }

    public void applyFoodDelta(int amount) {
        if (amount < 0 && -amount > food) {
            throw new IllegalArgumentException("Not enough food");
        }
        food += amount;
    }

    public void addPrestigePoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
        prestigePoints += amount;
    }

    public boolean canAfford(int amount) {
        return food >= Math.abs(amount);
    }

    public void spendPrestigePoints(int amount) {
        prestigePoints -= Math.abs(amount);
    }

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
