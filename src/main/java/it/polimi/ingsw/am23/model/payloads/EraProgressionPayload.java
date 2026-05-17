package it.polimi.ingsw.am23.model.payloads;

import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.state.CardState;

import java.io.Serializable;
import java.util.List;

public record EraProgressionPayload(Era newEra,
                                    List<String> newBuildingIdsInUpperRow,
                                    List<String> discardedBuildingIds,
                                    List<CardState> newBuildingCards) implements Serializable {
}
