package it.polimi.ingsw.am23.network.socket.server;

import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.payloads.*;
import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.VirtualServer;
import it.polimi.ingsw.am23.network.VirtualView;
import it.polimi.ingsw.am23.network.socket.messages.Message;
import it.polimi.ingsw.am23.network.socket.messages.request.*;
import it.polimi.ingsw.am23.network.socket.messages.response.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Per-client connection handler on the server side.
 * Implements {@link VirtualView} to ship server callbacks to the client
 * and {@link Runnable} to consume requests off the client socket on a
 * dedicated thread.
 * <p>
 * Outbound messages are dispatched through a single-threaded executor so a
 * slow or stuck client does not block the controller (which would otherwise
 * keep its lock held during the broadcast and freeze the whole match).
 */
public final class Connector implements VirtualView, Runnable {

    private final Socket clientSocket;  // to communicate with the client
    private final VirtualServer serverController;
    private final ExecutorService sendExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "connector-sender");
        t.setDaemon(true);
        return t;
    });
    private ObjectOutputStream out;
    private volatile boolean sendingEnabled = true;
    private String connectedPlayerId;
    private boolean explicitDisconnect = false;

    /**
     * Builds a new connector for the supplied client socket.
     *
     * @param clientSocket     accepted client socket
     * @param serverController controller the calls are delegated to
     * @throws IOException if the streams cannot be opened
     */
    public Connector(Socket clientSocket, VirtualServer serverController) throws IOException {
        this.clientSocket = clientSocket;
        this.serverController = serverController;
//        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
//        this.in  = new ObjectInputStream(clientSocket.getInputStream());
    }

    @Override
    public void run() {
        try {
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            while (!clientSocket.isClosed()) {
                Message message = (Message) in.readObject();
                dispatch(message);
            }
        } catch (IOException e) { // communication error
            System.err.println("<Connector>: connection closed –> " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("<Connector>: unknown class –> " + e.getMessage());
        } catch (Exception e) { // any other error not handled by dispatch
            System.err.println("<Connector>: error –> " + e.getMessage());
            e.printStackTrace();
        } finally {
            handleUnexpectedDisconnect();
            close();
        }
    }

    private void dispatch(Message message) throws Exception {
        if (message instanceof ConnectMessage m) {
            try {
                serverController.connect(m.getPlayerName(), this);
            } catch (Exception e) {
                onConnectError(e.getMessage());
            }

        } else if (message instanceof RefreshLobbyListMessage m) {
            try {
                serverController.requestLobbyList(m.getPlayerId());
            } catch (IllegalArgumentException | IllegalStateException e) {
                onActionError(ActionType.GENERIC, e.getMessage());
            }
        } else if (message instanceof CreateLobbyMessage m) {
            try {
                serverController.createLobby(m.getPlayerId(), m.getLobbyName(), m.getMaxPlayers());
            } catch (IllegalArgumentException | IllegalStateException e) {
                onActionError(ActionType.GENERIC, e.getMessage());
            }
        } else if (message instanceof JoinLobbyMessage m) {
            try {
                serverController.joinLobby(m.getPlayerId(), m.getLobbyId());
            } catch (IllegalArgumentException e) {
                onJoinError(e.getMessage());  // "Lobby not found: YYYY"
            } catch (IllegalStateException e) {
                onActionError(ActionType.GENERIC, e.getMessage());
            }

        } else {
            if (message instanceof LeaveLobbyMessage m) {
                try {
                    serverController.leaveLobby(m.getPlayerId(), m.getLobbyId());
                } catch (IllegalArgumentException | IllegalStateException e) {
                    onActionError(ActionType.GENERIC, e.getMessage());
                }

            } else if (message instanceof StartGameMessage m) {
                try {
                    serverController.startGame(m.getPlayerId(), m.getLobbyId());
                } catch (IllegalArgumentException | IllegalStateException e) {
                    onActionError(ActionType.GENERIC, e.getMessage());
                }

            } else if (message instanceof PlaceTotemMessage m) {
                try {
                    serverController.placeTotem(m.getPlayerId(), m.getOfferTileChar());
                } catch (IllegalArgumentException | IllegalStateException e) {
                    onActionError(ActionType.GENERIC, e.getMessage());
                }

            } else if (message instanceof TakeCardMessage m) {
                try {
                    serverController.takeSingleCard(m.getPlayerId(), m.getSelectedCard());
                } catch (RuntimeException e) {
                    onActionError(ActionType.TAKE_CARD, e.getMessage() != null ? e.getMessage() : e.toString());
                }
            } else if (message instanceof TakeExtraCardMessage m) {
                try {
                    serverController.takeExtraCard(m.getPlayerId(), m.getSelectedCardExtraDraw());
                } catch (RuntimeException e) {
                    onActionError(ActionType.TAKE_CARD, e.getMessage() != null ? e.getMessage() : e.toString());
                }

            } else if (message instanceof DisconnectMessage m) {
                explicitDisconnect = true;
                try {
                    serverController.disconnect(m.getPlayerId());
                } catch (Exception ignored) {
                }
            } else if (message instanceof SkipTurnMessage m) {
                try {
                    serverController.skipTurn(m.getPlayerId());
                } catch (IllegalArgumentException | IllegalStateException e) {
                    onActionError(ActionType.GENERIC, e.getMessage());
                }
            } else if (message instanceof RequestLeaderboardMessage m) {
                try {
                    serverController.requestLeaderboard(m.getPlayerId(), m.getPlayerCount());
                } catch (IllegalArgumentException | IllegalStateException e) {
                    onActionError(ActionType.GENERIC, e.getMessage());
                }
            } else {
                System.err.println("<Controller>: unknown message –>" + message.getClass().getName());
            }
        }
    }

    /**
     * Converts an unexpected socket close into the same controller-level
     * disconnect used by an explicit disconnect request.
     */
    private void handleUnexpectedDisconnect() {
        if (explicitDisconnect || connectedPlayerId == null) {
            return;
        }

        try {
            System.err.println("[Socket] Player disconnected unexpectedly: " + connectedPlayerId);
            serverController.disconnect(connectedPlayerId);
        } catch (Exception e) {
            System.err.println("[Socket] Error while disconnecting player "
                    + connectedPlayerId + ": " + e.getMessage());
        }
    }

    private void close() {
        sendingEnabled = false;
        sendExecutor.shutdownNow();
        try {
            clientSocket.close();
        } catch (IOException ignored) {
        }
    }

    // from controller to client

    /**
     * Hands the message off to the dedicated send thread so a slow client
     * never blocks the controller. The executor preserves submission order
     * because it is single-threaded; if a write fails, sending is disabled
     * and the connection is torn down.
     */
    private void send(Message message) {
        if (!sendingEnabled) return;
        sendExecutor.execute(() -> {
            if (!sendingEnabled) return;
            try {
                out.writeObject(message);
                out.flush();
                out.reset();
            } catch (IOException e) {
                if (sendingEnabled) {
                    sendingEnabled = false;
                    System.err.println("<Connector>: send failed, closing connection –> " + e.getMessage());
                    try {
                        clientSocket.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        });
    }

    /**
     * Sends the successful connection notification and stores the assigned
     * player id so an unexpected socket close can be translated into a
     * controller-level disconnect.
     *
     * @param playerId id assigned by the server
     * @param lobbies current lobby snapshot
     */
    @Override
    public void onConnected(String playerId, List<LobbyState> lobbies) {
        this.connectedPlayerId = playerId;
        send(new OnConnectedMessage(playerId, lobbies));
    }

    @Override
    public void onConnectError(String reason) {
        send(new OnConnectErrorMessage(reason));
    }

    @Override
    public void onLobbyListUpdated(List<LobbyState> lobbies) {
        send(new OnLobbyListUpdatedMessage(lobbies));
    }

    @Override
    public void onLobbyCreated(LobbyState lobby) {
        send(new OnLobbyCreatedMessage(lobby));
    }

    @Override
    public void onLobbyUpdate(LobbyState lobby) {
        send(new OnLobbyUpdateMessage(lobby));
    }

    @Override
    public void onJoinError(String reason) {
        send(new OnJoinErrorMessage(reason));
    }

    @Override
    public void onLobbyClosed() {
        send(new OnLobbyClosedMessage());
    }

    @Override
    public void onGameStarted(GameStartedPayload payload) throws IOException {
        send(new OnGameStartedMessage(payload));
    }

    @Override
    public void onTotemPlaced(TotemPlacedPayload payload) throws IOException {
        send(new OnTotemPlacedMessage(payload));
    }

    @Override
    public void onEndOfPlacingPhase(EndOfPlacingPhasePayload payload) throws IOException {
        send(new OnEndOfPlacingPhaseMessage(payload));
    }

    @Override
    public void onCardsTaken(CardsTakenPayload payload) throws IOException {
        send(new OnCardsTakenMessage(payload));
    }

    @Override
    public void onExtraDrawRequest(ExtraDrawRequestPayload payload) throws IOException {
        send(new OnExtraDrawRequestMessage(payload));
    }

    @Override
    public void onExtraCardTaken(ExtraCardTakenPayload payload) throws IOException {
        send(new OnExtraCardTakenMessage(payload));
    }

    @Override
    public void onEventResolved(EventResolvedPayload payload) throws IOException {
        send(new OnEventResolvedMessage(payload));
    }

    @Override
    public void onMarketRefreshed(MarketRefresherPayload payload) throws IOException {
        send(new OnMarketRefreshedMessage(payload));
    }

    @Override
    public void onEraProgression(EraProgressionPayload payload) throws IOException {
        send(new OnEraProgressionMessage(payload));
    }

    @Override
    public void onGameOver() throws IOException {
        send(new OnGameOverMessage());
    }

    @Override
    public void onScoreboardAvailable(ScoreBoardPayload payload) throws IOException {
        send(new OnScoreboardAvailableMessage(payload));
    }

    @Override
    public void onMatchRankingsAvailable(MatchRankingsPayload payload) {
        send(new OnMatchRankingsAvailableMessage(payload));
    }

    @Override
    public void onLeaderboardAvailable(LeaderboardPayload payload) {
        send(new OnLeaderboardAvailableMessage(payload));
    }

    @Override
    public void onActionError(ActionType actionType, String message) {
        send(new OnActionErrorMessage(actionType, message));
    }

    @Override
    public void onServerCrashed() {
    }
}
