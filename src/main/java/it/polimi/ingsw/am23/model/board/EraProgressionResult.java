package it.polimi.ingsw.am23.model.board;

import it.polimi.ingsw.am23.model.cards.BuildingCard;

import java.util.List;

public final class EraProgressionResult {

    private final List<BuildingCard> newTopBuildings;
    private final List<BuildingCard> discardedBuildings;

    public EraProgressionResult(List<BuildingCard> newTopBuildings, List<BuildingCard> discardedBuildings) {
        this.newTopBuildings = newTopBuildings;
        this.discardedBuildings = discardedBuildings;
    }

    public List<BuildingCard> newTopBuildings() { return newTopBuildings; }
    public List<BuildingCard> discardedBuildings() { return discardedBuildings; }
}

