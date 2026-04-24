package it.polimi.ingsw.am23.network.socket.messages.response;

import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class OnActionErrorMessage extends Message {

    private final ActionType actionType;
    private final String message;

    public OnActionErrorMessage(ActionType actionType, String message) {
        this.actionType = actionType;
        this.message = message;
    }

    public ActionType getActionType() { return actionType; }
    public String getMessage() { return message; }
}
