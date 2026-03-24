package it.polimi.ingsw.am23.model.cards;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;

import java.util.Objects;

public class BuildingCard extends Card {
    private final int foodCost;
    private final BuildingEffect effect;

    public BuildingCard(String id, Era era, int points, int foodCost, BuildingEffect effect) {
        super(id, era, points);
        this.foodCost = foodCost;
        this.effect = Objects.requireNonNull(effect, "effect cannot be null");
    }

    public int getFoodCost() {
        return foodCost;
    }
    public BuildingEffect getEffect() {
        return effect;
    }

    @Override
    public boolean canBeTaken(){
        return true;
    }

    @Override
    public void onTaken(Game game, Player player) {
        Objects.requireNonNull(game, "game cannot be null");
        Objects.requireNonNull(player, "player cannot be null");

        player.getTribe().addBuilding(this);
    }
}

