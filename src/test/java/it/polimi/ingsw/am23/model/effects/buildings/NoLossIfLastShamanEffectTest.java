package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.effects.ShamanRitualEffectData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NoLossIfLastShamanEffectTest {

    @Test
    void applyShamanRitualEnablesIgnoreLossFlag() {
        // Input  : empty ShamanRitualEffectData; call NoLossIfLastShamanEffect.applyShamanRitual.
        // Output : data.ignoreLoss()==true.
        ShamanRitualEffectData data = new ShamanRitualEffectData();
        new NoLossIfLastShamanEffect().applyShamanRitual(null, null, data);
        assertTrue(data.ignoreLoss());
    }
}
