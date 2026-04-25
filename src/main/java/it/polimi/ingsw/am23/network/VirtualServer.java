package it.polimi.ingsw.am23.network;

import it.polimi.ingsw.am23.model.cards.SelectedSingleCard;

public interface VirtualServer {

    // connection
    void connect(String playerName, VirtualView client) throws Exception;

    // lobby
    void createLobby(String playerId, String lobbyName, int maxPlayers) throws Exception;

    void joinLobby(String playerId, String lobbyId) throws Exception;

    void leaveLobby(String playerId, String lobbyId) throws Exception;

    void startGame(String playerId, String lobbyId) throws Exception;


    // game
    void placeTotem(String playerId, char offerTileChar) throws Exception;

    void takeSingleCard(String playerId, SelectedSingleCard selectedSingleCard) throws Exception;

    void takeExtraCard(String playerId, int index) throws Exception;
}
