package it.polimi.ingsw.am23.network.socket.messages.request;

import it.polimi.ingsw.am23.model.cards.SelectedCards;
import it.polimi.ingsw.am23.network.socket.messages.Message;

public final class TakeCardsMessage extends Message {

    private final String playerId;
    private final SelectedCards selectedCards;

    public TakeCardsMessage(String playerId, SelectedCards selectedCards) {
        this.playerId = playerId;
        this.selectedCards = selectedCards;
    }

    public String getPlayerId() { return playerId; }
    public SelectedCards getSelectedCards() { return selectedCards; }
}
