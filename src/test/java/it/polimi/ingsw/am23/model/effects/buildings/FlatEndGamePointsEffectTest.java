package it.polimi.ingsw.am23.model.effects.buildings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FlatEndGamePointsEffectTest {

    @Test
    void getEndGamePointsReturnsConfiguredFlatValue() {
        assertEquals(7, new FlatEndGamePointsEffect(7).getEndGamePoints(null, null));
    }
}
