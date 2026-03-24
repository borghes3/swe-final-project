package it.polimi.ingsw.am23.model.board;

import it.polimi.ingsw.am23.model.enums.Era;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefillResultTest {

    @Test
    void startsWithNoEraAdvance() {
        RefillResult result = new RefillResult();
        assertFalse(result.isEraAdvanced());
        assertNull(result.getNewEra());
    }

    @Test
    void registerEraAdvanceTracksHighestEra() {
        RefillResult result = new RefillResult();
        result.registerEraAdvance(Era.ERA_2);
        assertTrue(result.isEraAdvanced());
        assertEquals(Era.ERA_2, result.getNewEra());

        result.registerEraAdvance(Era.ERA_1);
        assertEquals(Era.ERA_2, result.getNewEra());

        result.registerEraAdvance(Era.ERA_3);
        assertEquals(Era.ERA_3, result.getNewEra());
    }
}
