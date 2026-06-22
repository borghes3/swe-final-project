package it.polimi.ingsw.am23.network.rmi.client;

import it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;
import it.polimi.ingsw.am23.network.VirtualServer;
import it.polimi.ingsw.am23.network.VirtualView;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * RMI-flavored {@link VirtualServer}.
 * Every method redeclares its parent to throw {@link RemoteException}, as
 * required by the {@link Remote} contract.
 */
public interface VirtualServerRmi extends VirtualServer, Remote {

    /** {@inheritDoc} */
    @Override
    void connect(String playerName, VirtualView client) throws RemoteException;

    /** {@inheritDoc} */
    @Override
    void createLobby(String playerId, String lobbyName, int maxPlayers) throws RemoteException;

    /** {@inheritDoc} */
    @Override
    void joinLobby(String playerId, String lobbyId) throws RemoteException;

    /** {@inheritDoc} */
    @Override
    void requestLobbyList(String playerId) throws RemoteException;

    /** {@inheritDoc} */
    @Override
    void leaveLobby(String playerId, String lobbyId) throws RemoteException;

    /** {@inheritDoc} */
    @Override
    void startGame(String playerId, String lobbyId) throws RemoteException;

    /** {@inheritDoc} */
    @Override
    void placeTotem(String playerId, char offerTileChar) throws RemoteException;

    /** {@inheritDoc} */
    @Override
    void takeSingleCard(String playerId, SelectedSingleCard selectedSingleCard) throws RemoteException;

    /** {@inheritDoc} */
    @Override
    void takeExtraCard(String playerId, SelectedCardExtraDraw selectedCardExtraDraw) throws RemoteException;

    /** {@inheritDoc} */
    @Override
    void skipTurn(String playerId) throws RemoteException;

    /** {@inheritDoc} */
    @Override
    void disconnect(String playerId) throws RemoteException;

    /** {@inheritDoc} */
    @Override
    void ping() throws RemoteException;

    /** {@inheritDoc} */
    @Override
    void requestLeaderboard(String playerId, int playerCount) throws RemoteException;
}
