package it.polimi.ingsw.am23.network.socket.client;

import it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;
import it.polimi.ingsw.am23.network.VirtualServer;
import it.polimi.ingsw.am23.network.VirtualView;
import it.polimi.ingsw.am23.network.socket.messages.Message;
import it.polimi.ingsw.am23.network.socket.messages.request.*;
import it.polimi.ingsw.am23.network.socket.messages.response.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Client-side socket transport.
 * Implements {@link VirtualServer} by serializing each request into a
 * typed {@link Message} and shipping it over an {@link ObjectOutputStream}.
 * Incoming server callbacks are dispatched to the local {@link VirtualView}
 * by a dedicated reader thread.
 */
public final class SocketClient implements VirtualServer {

    private final Socket socket;  // to communicate with the server
    private final VirtualView view;  // to communicate with the view
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    private SocketClient(Socket socket, VirtualView view) throws IOException {
        this.socket = socket;
        this.view = view;
        // output stream first to avoid the classic ObjectStream init deadlock
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Establishes the TCP connection to the server and starts the reader
     * thread.
     *
     * @param host server host name or IP
     * @param view local view to dispatch callbacks to
     * @return the resulting {@link VirtualServer} stub
     * @throws IOException on connection failure
     */
    public static VirtualServer connectToServer(String host, VirtualView view) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, 1235), 2000);
        SocketClient client = new SocketClient(socket, view);
        client.startListening();
        return client;
    }

    // callback from server to client

    // Listens for messages (from server) on a dedicated daemon thread.
    private void startListening() {
        Thread thread = new Thread(this::readLoop, "socket-reader");
        thread.setDaemon(true);
        thread.start();
    }

    private void readLoop() {
        try {
            while (!socket.isClosed()) {
                Message message = (Message) in.readObject();
                dispatch(message);
            }
        } catch (java.io.EOFException | SocketException e) {
            System.err.println("<SocketClient>: server lost – " + e.getMessage());
            notifyCrash();
        } catch (IOException e) {
            System.err.println("<SocketClient>: connection closed – " + e.getMessage());
            notifyCrash();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
    }

    private void notifyCrash() {
        try {
            view.onServerCrashed();
        } catch (Exception ignored) {
        }
    }

    private void dispatch(Message message) throws Exception {
        if (message instanceof OnConnectedMessage m) {
            view.onConnected(m.getPlayerId(), m.getLobbies());

        } else if (message instanceof OnConnectErrorMessage m) {
            view.onConnectError(m.getReason());

        } else if (message instanceof OnLobbyListUpdatedMessage m) {
            view.onLobbyListUpdated(m.getLobbies());

        } else if (message instanceof OnLobbyCreatedMessage m) {
            view.onLobbyCreated(m.getLobby());

        } else if (message instanceof OnLobbyUpdateMessage m) {
            view.onLobbyUpdate(m.getLobby());

        } else if (message instanceof OnJoinErrorMessage m) {
            view.onJoinError(m.getReason());

        } else if (message instanceof OnLobbyClosedMessage) {
            view.onLobbyClosed();

        } else if (message instanceof OnGameStartedMessage m) {
            view.onGameStarted(m.getPayload());

        } else if (message instanceof OnTotemPlacedMessage m) {
            view.onTotemPlaced(m.getPayload());

        } else if (message instanceof OnEndOfPlacingPhaseMessage m) {
            view.onEndOfPlacingPhase(m.getPayload());

        } else if (message instanceof OnCardsTakenMessage m) {
            view.onCardsTaken(m.getPayload());

        } else if (message instanceof OnExtraDrawRequestMessage m) {
            view.onExtraDrawRequest(m.getPayload());

        } else if (message instanceof OnExtraCardTakenMessage m) {
            view.onExtraCardTaken(m.getPayload());

        } else if (message instanceof OnEventResolvedMessage m) {
            view.onEventResolved(m.getPayload());

        } else if (message instanceof OnMarketRefreshedMessage m) {
            view.onMarketRefreshed(m.getPayload());

        } else if (message instanceof OnEraProgressionMessage m) {
            view.onEraProgression(m.getPayload());

        } else if (message instanceof OnGameOverMessage) {
            view.onGameOver();

        } else if (message instanceof OnScoreboardAvailableMessage m) {
            view.onScoreboardAvailable(m.getPayload());

        } else if (message instanceof OnMatchRankingsAvailableMessage m) {
            view.onMatchRankingsAvailable(m.getPayload());

        } else if (message instanceof OnLeaderboardAvailableMessage m) {
            view.onLeaderboardAvailable(m.getPayload());

        } else if (message instanceof OnActionErrorMessage m) {
            view.onActionError(m.getActionType(), m.getMessage());

        } else {
            System.err.println("SocketClient: unknown message – " + message.getClass().getName());
        }
    }

    private void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }


    // requests from view to server

    private synchronized void send(Message message) throws Exception {
        try {
            out.writeObject(message);
            out.flush();
            out.reset();
        } catch (IOException e) {
            throw new Exception("Failed to send message to server: " + e.getMessage(), e);
        }
    }

    @Override
    public void connect(String playerName, VirtualView client) throws Exception {
        send(new ConnectMessage(playerName));
    }

    @Override
    public void createLobby(String playerId, String lobbyName, int maxPlayers) throws Exception {
        send(new CreateLobbyMessage(playerId, lobbyName, maxPlayers));
    }

    @Override
    public void joinLobby(String playerId, String lobbyId) throws Exception {
        send(new JoinLobbyMessage(playerId, lobbyId));
    }

    @Override
    public void leaveLobby(String playerId, String lobbyId) throws Exception {
        send(new LeaveLobbyMessage(playerId, lobbyId));
    }

    @Override
    public void startGame(String playerId, String lobbyId) throws Exception {
        send(new StartGameMessage(playerId, lobbyId));
    }

    @Override
    public void placeTotem(String playerId, char offerTileChar) throws Exception {
        send(new PlaceTotemMessage(playerId, offerTileChar));
    }

    @Override
    public void takeSingleCard(String playerId, SelectedSingleCard selectedSingleCard) throws Exception {
        send(new TakeCardMessage(playerId, selectedSingleCard));
    }

    @Override
    public void takeExtraCard(String playerId, SelectedCardExtraDraw selectedCardExtraDraw) throws Exception {
        send(new TakeExtraCardMessage(playerId, selectedCardExtraDraw));
    }

    @Override
    public void skipTurn(String playerId) throws Exception {
        send(new SkipTurnMessage(playerId));
    }

    @Override
    public void disconnect(String playerId) throws Exception {
        send(new DisconnectMessage(playerId));
    }

    @Override
    public void requestLobbyList(String playerId) throws Exception {
        send(new RefreshLobbyListMessage(playerId));
    }

    @Override
    public void requestLeaderboard(String playerId, int playerCount) throws Exception {
        send(new RequestLeaderboardMessage(playerId, playerCount));
    }
}

