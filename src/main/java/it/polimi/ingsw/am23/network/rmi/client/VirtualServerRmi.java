package it.polimi.ingsw.am23.network.rmi.client;

import it.polimi.ingsw.am23.model.cards.SelectedCards;
import it.polimi.ingsw.am23.network.VirtualServer;
import it.polimi.ingsw.am23.network.VirtualView;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface VirtualServerRmi extends VirtualServer, Remote {

    @Override
    void connect(String playerName, VirtualView client) throws RemoteException;

    @Override
    void createLobby(String playerId, String lobbyName, int maxPlayers) throws RemoteException;

    @Override
    void joinLobby(String playerId, String lobbyId) throws RemoteException;

    @Override
    void leaveLobby(String playerId, String lobbyId) throws RemoteException;

    @Override
    void placeTotem(String playerId, char offerTileChar) throws RemoteException;

    @Override
    void takeCards(String playerId, SelectedCards selectedCards) throws RemoteException;

    @Override
    void takeExtraCard(String playerId, int index) throws RemoteException;
}
