package it.polimi.ingsw.am23.model.board;

import it.polimi.ingsw.am23.model.cards.BuildingCard;

import java.util.List;

/**
 * Outcome of an era progression performed by {@link
 * CardMarket#handleEraProgression}. Carries the new top buildings and the
 * buildings discarded as part of the progression.
 */
public final class EraProgressionResult {

    private final List<BuildingCard> newTopBuildings;
    private final List<BuildingCard> discardedBuildings;

    /**
     * Builds a new era progression result.
     *
     * @param newTopBuildings    the new top row buildings after the progression
     * @param discardedBuildings the buildings discarded by the progression
     */
    public EraProgressionResult(List<BuildingCard> newTopBuildings, List<BuildingCard> discardedBuildings) {
        this.newTopBuildings = newTopBuildings;
        this.discardedBuildings = discardedBuildings;
    }

    /** @return the buildings now visible in the top building row */
    public List<BuildingCard> newTopBuildings() { return newTopBuildings; }
    /** @return the buildings discarded by the progression (only at Era 3) */
    public List<BuildingCard> discardedBuildings() { return discardedBuildings; }
}

