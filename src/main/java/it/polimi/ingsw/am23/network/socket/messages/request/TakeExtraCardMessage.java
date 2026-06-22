package it.polimi.ingsw.am23.network.socket.messages.request;

import it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class TakeExtraCardMessage extends Message {

    private final String playerId;
    private final SelectedCardExtraDraw selectedCardExtraDraw;

    public TakeExtraCardMessage(String playerId, SelectedCardExtraDraw selectedCardExtraDraw) {
        this.playerId = playerId;
        this.selectedCardExtraDraw = selectedCardExtraDraw;
    }

    public String getPlayerId() {
        return playerId;
    }

    public SelectedCardExtraDraw getSelectedCardExtraDraw() {
        return selectedCardExtraDraw;
    }
}
