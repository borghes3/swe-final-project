package it.polimi.ingsw.am23.model.effects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShamanRitualEffectDataTest {

    @Test
    void settersUpdateShamanRitualFlagsAndBonus() {
        // Input  : setBonusStars(3), setIgnoreLoss(true), setDoubleWin(true).
        // Output : getBonusStars()==3, ignoreLoss()==true, doubleWin()==true.
        ShamanRitualEffectData data = new ShamanRitualEffectData();
        data.setBonusStars(3);
        data.setIgnoreLoss(true);
        data.setDoubleWin(true);

        assertEquals(3, data.getBonusStars());
        assertTrue(data.ignoreLoss());
        assertTrue(data.doubleWin());
    }
}
