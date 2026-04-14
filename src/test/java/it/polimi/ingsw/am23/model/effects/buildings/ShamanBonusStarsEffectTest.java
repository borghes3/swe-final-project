package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.effects.ShamanRitualEffectData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShamanBonusStarsEffectTest {

    @Test
    void applyShamanRitualSetsConfiguredBonusStars() {
        ShamanRitualEffectData data = new ShamanRitualEffectData();
        new ShamanBonusStarsEffect(4).applyShamanRitual(null, null, data);
        assertEquals(4, data.getBonusStars());
    }
}
