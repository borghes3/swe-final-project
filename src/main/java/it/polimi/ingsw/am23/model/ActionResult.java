package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.model.state.GameState;

public final class ActionResult {
    private GameState state;

    public ActionResult(GameState state) {
        this.state = state;
    }

}
