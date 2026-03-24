package it.polimi.ingsw.am23.model.player;

import java.util.Objects;

public class Totem {
    private final String ownerId;
    private final String color;

    public Totem(String ownerId, String color) {
        this.ownerId = Objects.requireNonNull(ownerId);
        this.color = color;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getColor() {
        return color;
    }
}
