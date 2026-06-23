package it.polimi.ingsw.am23.network;

import it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;

/**
 * Remote server contract exposed to clients. Each method represents an
 * action a client can perform, from connection setup to in-game actions.
 */
public interface VirtualServer {

    /**
     * Performs a connection setup, registering the client and returning a
     * unique identifier through {@link VirtualView#onConnected}.
     *
     * @param playerName desired display nickname
     * @param client     callback view used to push notifications back
     * @throws Exception on transport failure
     */
    void connect(String playerName, VirtualView client) throws Exception;

    /**
     * Creates a new lobby owned by the supplied player.
     *
     * @param playerId   id of the player creating the lobby
     * @param lobbyName  human readable name of the lobby
     * @param maxPlayers maximum number of players allowed
     * @throws Exception on transport failure
     */
    void createLobby(String playerId, String lobbyName, int maxPlayers) throws Exception;

    /**
     * Joins an existing lobby.
     *
     * @param playerId id of the player
     * @param lobbyId  id of the lobby to join
     * @throws Exception on transport failure
     */
    void joinLobby(String playerId, String lobbyId) throws Exception;

    /**
     * Requests an up-to-date snapshot of the lobby list.
     *
     * @param playerId id of the requesting player
     * @throws Exception on transport failure
     */
    void requestLobbyList(String playerId) throws Exception;

    /**
     * Leaves a lobby the player is currently in.
     *
     * @param playerId id of the player
     * @param lobbyId  id of the lobby to leave
     * @throws Exception on transport failure
     */
    void leaveLobby(String playerId, String lobbyId) throws Exception;

    /**
     * Starts the game for the supplied lobby (only the owner is allowed).
     *
     * @param playerId id of the requesting player
     * @param lobbyId  id of the lobby
     * @throws Exception on transport failure
     */
    void startGame(String playerId, String lobbyId) throws Exception;


    /**
     * Places the player's totem on an offer tile.
     *
     * @param playerId      id of the player
     * @param offerTileChar letter of the offer tile
     * @throws Exception on transport failure
     */
    void placeTotem(String playerId, char offerTileChar) throws Exception;

    /**
     * Draws a single card from the card market.
     *
     * @param playerId           id of the player
     * @param selectedSingleCard the selected card
     * @throws Exception on transport failure
     */
    void takeSingleCard(String playerId, SelectedSingleCard selectedSingleCard) throws Exception;

    /**
     * Performs an extra draw on behalf of the entitled player.
     *
     * @param playerId              id of the player
     * @param selectedCardExtraDraw the selected card for the extra draw
     * @throws Exception on transport failure
     */
    void takeExtraCard(String playerId, SelectedCardExtraDraw selectedCardExtraDraw) throws Exception;

    /**
     * Skips the player's draw turn.
     *
     * @param playerId id of the player
     * @throws Exception on transport failure
     */
    void skipTurn(String playerId) throws Exception;

    /**
     * Disconnects the player.
     *
     * @param playerId id of the player
     * @throws Exception on transport failure
     */
    void disconnect(String playerId) throws Exception;

    /**
     * Requests the global leaderboard for a given player-count slice.
     *
     * @param playerId    id of the requesting player
     * @param playerCount player-count slice to query
     * @throws Exception on transport failure
     */
    void requestLeaderboard(String playerId, int playerCount) throws Exception;
}
