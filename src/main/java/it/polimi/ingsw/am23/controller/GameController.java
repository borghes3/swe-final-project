package it.polimi.ingsw.am23.controller;

import it.polimi.ingsw.am23.model.cards.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.model.cards.SelectedCards;

public interface GameController {
    void placeTotem(char offerTileChar);
    void takeCards(SelectedCards selectedCards);
    void takeExtraCard(SelectedCardExtraDraw selectedCardExtraDraw);
    void close();
}