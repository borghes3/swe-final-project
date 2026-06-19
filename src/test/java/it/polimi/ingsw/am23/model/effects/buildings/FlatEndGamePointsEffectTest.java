package it.polimi.ingsw.am23.model.effects.buildings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlatEndGamePointsEffectTest {

    @Test
    void getEndGamePointsReturnsConfiguredFlatValue() {
        // Input  : FlatEndGamePointsEffect(value=7); call getEndGamePoints(null, null).
        // Output : 7 (the configured flat value, independent of game/player).
        assertEquals(7, new FlatEndGamePointsEffect(7).getEndGamePoints(null, null));
    }
}
