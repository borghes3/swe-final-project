package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.model.enums.ActionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActionResultTest {

    @Test
    void successAndFailureFactoriesSetSemanticFields() {
        // Input  : ActionResult.success(TAKE_CARD, "ok") and ActionResult.failure(PLACE_TOTEM, WRONG_PLAYER, "no").
        // Output : success.isSuccess()==true with error==NONE; failure.isSuccess()==false with error==WRONG_PLAYER.
        ActionResult success = ActionResult.success(ActionType.TAKE_CARD, "ok");
        ActionResult failure = ActionResult.failure(ActionType.PLACE_TOTEM, ErrorCode.WRONG_PLAYER, "no");

        assertTrue(success.isSuccess());
        assertEquals(ErrorCode.NONE, success.getError());
        assertFalse(failure.isSuccess());
        assertEquals(ErrorCode.WRONG_PLAYER, failure.getError());
    }
}
