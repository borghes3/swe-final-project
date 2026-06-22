package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.model.enums.ActionType;

import java.util.Objects;

/**
 * Immutable response returned by the {@link GameModel} for every player
 * action. Carries the originating {@link ActionType}, a success flag, an
 * {@link ErrorCode} (always {@link ErrorCode#NONE} on success) and a human
 * readable message.
 */
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

    /**
     * Convenience factory for a successful result.
     *
     * @param actionType the action that produced this result
     * @param message    human readable message
     * @return the success result
     */
    public static ActionResult success(ActionType actionType, String message) {
        return new ActionResult(actionType, true, ErrorCode.NONE, message);
    }

    /**
     * Convenience factory for a failure result.
     *
     * @param actionType the action that produced this result
     * @param error      machine readable error code
     * @param message    human readable error message
     * @return the failure result
     */
    public static ActionResult failure(ActionType actionType, ErrorCode error, String message) {
        return new ActionResult(actionType, false, error, message);
    }

    /** @return the action that produced this result */
    public ActionType getActionType() {
        return actionType;
    }

    /** @return {@code true} if the action succeeded */
    public boolean isSuccess() {
        return success;
    }

    /** @return the error code; always {@link ErrorCode#NONE} on success */
    public ErrorCode getError() {
        return error;
    }

    /** @return the human readable message */
    public String getMessage() {
        return message;
    }
}
