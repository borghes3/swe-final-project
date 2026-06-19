package it.polimi.ingsw.am23.model.board;

import it.polimi.ingsw.am23.model.enums.Era;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefillResultTest {

    @Test
    void registerEraAdvanceTracksHighestEraOnly() {
        // Input  : RefillResult.registerEraAdvance with sequence [ERA_2, ERA_1, ERA_3].
        // Output : isEraAdvanced()==true and getNewEra()==ERA_3 (highest seen).
        RefillResult result = new RefillResult();

        result.registerEraAdvance(Era.ERA_2);
        result.registerEraAdvance(Era.ERA_1);
        result.registerEraAdvance(Era.ERA_3);

        assertTrue(result.isEraAdvanced());
        assertEquals(Era.ERA_3, result.getNewEra());
    }
}
