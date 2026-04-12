package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.model.enums.ActionType;

import java.util.Objects;

public final class ActionResult {

    private final ActionType actionType;
    private final boolean success;
    private final ErrorCode error;
    private final String message;

    private ActionResult(ActionType actionType, boolean success, ErrorCode error, String message) {
        this.actionType = Objects.requireNonNull(actionType, "actionType cannot be null");
        this.success = success;
        this.error = Objects.requireNonNull(error, "error cannot be null");
        this.message = message;
    }

    public static ActionResult success(ActionType actionType, String message) {
        return new ActionResult(actionType, true, ErrorCode.NONE, message);
    }

    public static ActionResult failure(ActionType actionType, ErrorCode error, String message) {
        return new ActionResult(actionType, false, error, message);
    }

    public ActionType getActionType() {
        return actionType;
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
}