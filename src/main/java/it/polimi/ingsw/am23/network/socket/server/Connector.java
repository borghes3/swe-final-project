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

public final class Connector implements VirtualView, Runnable {

    private final Socket clientSocket;  // to communicate with the client
    private final VirtualServer serverController;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    /*
     * The connector learns the playerId only after the controller calls
     * onConnected(...). We store it so that an unexpected socket closure can be
     * propagated to the application controller.
     */
    private volatile String connectedPlayerId;

    /*
     * Avoids calling serverController.disconnect(...) twice:
     * once for an explicit DisconnectMessage and once in the run() finally block.
     */
    private volatile boolean disconnectAlreadyHandled;

    public Connector(Socket clientSocket, VirtualServer serverController) throws IOException {
        this.clientSocket = clientSocket;
        this.serverController = serverController;
        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.in  = new ObjectInputStream(clientSocket.getInputStream());
    }

    // from client to controller
    @Override
    public void run() {
        try {
            while (!clientSocket.isClosed()) {
                Message message = (Message) in.readObject();
                dispatch(message);
            }

        } catch (IOException e) {
            if (!disconnectAlreadyHandled) {
                System.err.println("<Connector>: connection closed -> " + e.getMessage());
            }

        } catch (ClassNotFoundException e) {
            System.err.println("<Connector>: unknown message class -> " + e.getMessage());

        } catch (Exception e) {
            System.err.println("<Connector>: unexpected error -> " + e.getMessage());
            e.printStackTrace();

        } finally {
            handleUnexpectedDisconnect();
            close();
        }
    }

    private void dispatch(Message message) throws Exception {
        if (message instanceof ConnectMessage m) {
        try{
            serverController.connect(m.getPlayerName(), this);
        } catch (Exception e){
            onConnectError(e.getMessage());
        }

        } else if (message instanceof RefreshLobbyListMessage m) {
            try{
                serverController.requestLobbyList(m.getPlayerId());
            } catch (IllegalArgumentException | IllegalStateException e){
                onActionError(ActionType.GENERIC, e.getMessage());
            }
        } else if (message instanceof CreateLobbyMessage m) {
            try {
                serverController.createLobby(m.getPlayerId(), m.getLobbyName(), m.getMaxPlayers());
            }catch (IllegalArgumentException | IllegalStateException e){
                onActionError(ActionType.GENERIC, e.getMessage());
            }
        } else if (message instanceof JoinLobbyMessage m) {
            try {
                serverController.joinLobby(m.getPlayerId(), m.getLobbyId());
            } catch (IllegalArgumentException e) {
                onJoinError(e.getMessage());  // "Lobby not found: YYYY"
            } catch (IllegalStateException e){
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
                } catch (IllegalArgumentException | IllegalStateException e){
                    onActionError(ActionType.GENERIC, e.getMessage());
                }

            } else if (message instanceof TakeCardMessage m) {
                try {
                    serverController.takeSingleCard(m.getPlayerId(), m.getSelectedCard());
                } catch (IllegalArgumentException | IllegalStateException | IndexOutOfBoundsException e){
                    onActionError(ActionType.GENERIC, e.getMessage());
                }
            } else if (message instanceof TakeExtraCardMessage m) {
                try {
                    serverController.takeExtraCard(m.getPlayerId(), m.getSelectedCardExtraDraw());
                } catch (IllegalArgumentException | IllegalStateException e){
                    onActionError(ActionType.GENERIC, e.getMessage());
                }

            } else if (message instanceof DisconnectMessage m) {
                String playerId = m.getPlayerId() != null ? m.getPlayerId() : connectedPlayerId;
                markDisconnected(playerId);

                try {
                    if (playerId != null) {
                        serverController.disconnect(playerId);
                    }
                } catch (Exception e) {
                    System.err.println("<Connector>: error during explicit disconnect -> " + e.getMessage());
                } finally {
                    close();
                }
            } else if (message instanceof SkipTurnMessage m) {
                try {
                    serverController.skipTurn(m.getPlayerId());
                } catch (IllegalArgumentException | IllegalStateException e) {
                    onActionError(ActionType.GENERIC, e.getMessage());
                }
            }
            else {
                System.err.println("<Controller>: messaggio sconosciuto –>" + message.getClass().getName());
            }
        }
    }

    private void close() {
        try {
            in.close();
        } catch (IOException ignored) {
        }

        try {
            out.close();
        } catch (IOException ignored) {
        }

        try {
            clientSocket.close();
        } catch (IOException ignored) {
        }
    }

    private void handleUnexpectedDisconnect() {
        String playerId = connectedPlayerId;

        if (playerId == null || disconnectAlreadyHandled) {
            return;
        }

        markDisconnected(playerId);

        try {
            serverController.disconnect(playerId);
        } catch (Exception e) {
            System.err.println("<Connector>: error while handling unexpected disconnect for player "
                    + playerId + " -> " + e.getMessage());
        }
    }

    private void markDisconnected(String playerId) {
        disconnectAlreadyHandled = true;

        if (playerId != null) {
            connectedPlayerId = playerId;
        }
    }

    // from controller to client

    private synchronized void send(Message message) throws IOException {
        try {
            out.writeObject(message);
            out.flush();
            out.reset();
        } catch (IOException e) {
            throw new IOException("Impossibile inviare il messaggio al client: " + e.getMessage(), e);
        }
    }

    @Override
    public void onConnected(String playerId, List<LobbyState> lobbies) throws IOException {
        this.connectedPlayerId = playerId;
        send(new OnConnectedMessage(playerId, lobbies));
    }

    @Override
    public void onConnectError(String reason) throws IOException {
        send(new OnConnectErrorMessage(reason));
    }

    @Override
    public void onLobbyListUpdated(List<LobbyState> lobbies) throws IOException {
        send(new OnLobbyListUpdatedMessage(lobbies));
    }

    @Override
    public void onLobbyCreated(LobbyState lobby) throws IOException {
        send(new OnLobbyCreatedMessage(lobby));
    }

    @Override
    public void onLobbyUpdate(LobbyState lobby) throws IOException {
        send(new OnLobbyUpdateMessage(lobby));
    }

    @Override
    public void onJoinError(String reason) throws IOException {
        send(new OnJoinErrorMessage(reason));
    }

    @Override
    public void onLobbyClosed() throws IOException {
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
    public void onActionError(ActionType actionType, String message) throws IOException {
        send(new OnActionErrorMessage(actionType, message));
    }

    @Override
    public void onServerCrashed() {}
}
