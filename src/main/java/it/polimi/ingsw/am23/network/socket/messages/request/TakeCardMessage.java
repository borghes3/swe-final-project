package it.polimi.ingsw.am23.network.socket.messages.request;

import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class TakeCardMessage extends Message {

    private final String playerId;
    private final SelectedSingleCard selectedCard;

    public TakeCardMessage(String playerId, SelectedSingleCard selectedCard) {
        this.playerId = playerId;
        this.selectedCard = selectedCard;
    }

    public String getPlayerId() {
        return playerId;
    }

    public SelectedSingleCard getSelectedCard() {
        return selectedCard;
    }
}
