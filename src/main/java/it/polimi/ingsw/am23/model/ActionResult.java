package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.model.enums.ActionError;
import it.polimi.ingsw.am23.model.enums.ActionType;

public final class ActionResult {

    private final ActionType actionType;
    private final boolean success;
    private final ErrorCode error;
    private final String message;

    public ActionResult(ActionType actionType, boolean success, ErrorCode error, String message) {
        this.actionType = actionType;
        this.success = success;
        this.error = error;
        this.message = message;
    }

    public static ActionResult success(ActionType actionType, String message) {
        return new ActionResult(actionType, true, ErrorCode.NONE, message);
    }

    public static ActionResult failure(ActionType actionType, String message) {
        return new ActionResult(actionType, false, ErrorCode.NONE, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public ErrorCode getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public ActionType getActionType() {
        return actionType;
    }

}
