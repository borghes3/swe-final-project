package it.polimi.ingsw.am23.model.board;

import it.polimi.ingsw.am23.model.cards.BuildingCard;

import java.util.List;

/**
 * Outcome of an era progression performed by {@link
 * CardMarket#handleEraProgression}. Carries the new top buildings and the
 * buildings discarded as part of the progression.
 */
public record EraProgressionResult(List<BuildingCard> newTopBuildings, List<BuildingCard> discardedBuildings) {

    /**
     * Builds a new era progression result.
     *
     * @param newTopBuildings    the new top row buildings after the progression
     * @param discardedBuildings the buildings discarded by the progression
     */
    public EraProgressionResult {
    }

    /**
     * @return the buildings now visible in the top building row
     */
    @Override
    public List<BuildingCard> newTopBuildings() {
        return newTopBuildings;
    }

    /**
     * @return the buildings discarded by the progression (only at Era 3)
     */
    @Override
    public List<BuildingCard> discardedBuildings() {
        return discardedBuildings;
    }
}

