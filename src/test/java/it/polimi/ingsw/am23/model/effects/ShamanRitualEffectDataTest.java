package it.polimi.ingsw.am23.model.effects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShamanRitualEffectDataTest {

    @Test
    void settersUpdateShamanRitualFlagsAndBonus() {
        ShamanRitualEffectData data = new ShamanRitualEffectData();
        data.setBonusStars(3);
        data.setIgnoreLoss(true);
        data.setDoubleWin(true);

        assertEquals(3, data.getBonusStars());
        assertTrue(data.ignoreLoss());
        assertTrue(data.doubleWin());
    }
}
