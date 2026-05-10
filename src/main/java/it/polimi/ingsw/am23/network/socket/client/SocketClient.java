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
import java.net.SocketTimeoutException;

public final class SocketClient implements VirtualServer {

    private final Socket socket;  // to communicate with the server
    private final VirtualView view;  // to communicate with the view
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    private SocketClient(Socket socket, VirtualView view) throws IOException {
        this.socket = socket;
        this.view   = view;
        // possibile deadlock
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in  = new ObjectInputStream(socket.getInputStream());
    }

    // to create a connection to the socket (different from connect() method)
    // in RMI they are one nested into the other
    public static VirtualServer connectToServer(String host, VirtualView view) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, 1235), 2000);
        SocketClient client = new SocketClient(socket, view);
        client.startListening();
        return client;
    }

    // callback from server to client

    // listen for messages (from server) on a different thread
    private void startListening() {
        Thread thread = new Thread(this::readLoop, "socket-reader");  // socket-reader is the name of the thread, useful for debugging
        thread.setDaemon(true);  // the thread will be destroyed when the program exits (the client doesn't freeze when it disconnects)
        thread.start();
    }

    private void readLoop() {
        try {
            while (!socket.isClosed()) {
                Message message = (Message) in.readObject();
                dispatch(message);
            }
        }catch (java.io.EOFException | SocketException e) {
            System.err.println("<SocketClient>: server caduto – " + e.getMessage());
            notifyCrash();
        }catch (IOException e) {
            System.err.println("<SocketClient>: connessione chiusa – " + e.getMessage());
            notifyCrash();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
    }

    private void notifyCrash(){
        try{
            view.onServerCrashed();
        } catch(Exception ignored){}
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
            view.onGameStarted(m.getGameState());

        } else if (message instanceof OnGameStateChangedMessage m) {
            view.onGameStateChanged(m.getGameState());

        } else if (message instanceof OnEndOfPlacingPhaseMessage m) {
            view.onEndOfPlacingPhase(m.getGameState());

        } else if (message instanceof OnEndOfDrawingPhaseMessage m) {
            view.onEndOfDrawingPhase(m.getGameState());

        } else if (message instanceof OnExtraDrawRequestMessage m) {
            view.onExtraDrawRequest(m.getGameState());

        } else if (message instanceof OnEndOfResolvingPhaseMessage m) {
            view.onEndOfResolvingPhase(m.getGameState());

        } else if (message instanceof OnEraProgressionMessage m) {
            view.onEraProgression(m.getGameState());

        } else if (message instanceof OnGameOverMessage m) {
            view.onGameOver(m.getGameState());

        } else if (message instanceof OnScoreboardAvailableMessage m) {
            view.onScoreboardAvailable(m.getGameState());

        } else if (message instanceof OnActionErrorMessage m) {
            view.onActionError(m.getActionType(), m.getMessage());

        } else {
            System.err.println("SocketClient: messaggio sconosciuto – " + message.getClass().getName());
        }
    }

    private void close() {
        try {
            socket.close();
        } catch (IOException ignored) {}
    }


    // requests from view to server

    private synchronized void send(Message message) throws Exception {
        try {
            out.writeObject(message);
            out.flush();
            out.reset();
        } catch (IOException e) {
            throw new Exception("Impossibile inviare il messaggio al server: " + e.getMessage(), e);
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
    public void ping() throws Exception{}
    // socket rileva il crash dalla readLoop, non serve il ping
}
