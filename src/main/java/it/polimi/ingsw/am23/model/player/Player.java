package it.polimi.ingsw.am23.model.player;

import java.util.Objects;

public class Player {

    private final String id;
    private final String nickname;
    private int food;
    private int prestigePoints;
    private final Tribe tribe;
    private final Totem totem;

    public Player(String id, String nickname, int food, int prestigePoints, String totemColor) {
        this.id = Objects.requireNonNull(id);
        this.nickname = Objects.requireNonNull(nickname);

        if (food < 0) {
            throw new IllegalArgumentException("initialFood or prestigePoints cannot be negative");
        }

        this.food = food;
        this.prestigePoints = prestigePoints;
        this.tribe = new Tribe();
        this.totem = new Totem(id, totemColor);
    }

    public String getId() {
        return id;
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

    public Tribe getTribe() {
        return tribe;
    }

    public Totem getTotem() {
        return totem;
    }

    public void addFood(int amount){
        if(amount < 0){
            throw new IllegalArgumentException("amount cannot be negative");
        }
        food += amount;
    }

    public void addPrestigePoints(int amount){
        if(amount < 0){
            throw new IllegalArgumentException("amount cannot be negative");
        }
        prestigePoints += amount;
    }

    public void spendFood(int amount){
        if(amount < 0){
            throw new IllegalArgumentException("amount cannot be negative");
        }
        if(amount > food){
            throw new IllegalArgumentException("Not enough food");
        }
        food -= amount;
    }

    public boolean canAfford(int amount){
        if(amount < 0){
            throw new IllegalArgumentException("amount cannot be negative");
        }
        return food >= amount;
    }

    public void spendPrestigePoints(int amount){
        if(amount < 0){
            throw new IllegalArgumentException("amount cannot be negative");
        }
        prestigePoints -= amount;
    }

    public void losePrestigePoints(int amount){
        if(amount < 0){
            throw new IllegalArgumentException("amount cannot be negative");
        }
        prestigePoints -= amount;
    }
}
