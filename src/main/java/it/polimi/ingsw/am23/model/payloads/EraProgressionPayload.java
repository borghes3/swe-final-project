package it.polimi.ingsw.am23.model.payloads;

import it.polimi.ingsw.am23.model.enums.Era;

import java.util.List;
import java.util.Optional;

public record EraProgressionPayload(Era newEra, List<String> newBuildingIdsInUpperRow, List<String> discardedBuildingIds) {
}
