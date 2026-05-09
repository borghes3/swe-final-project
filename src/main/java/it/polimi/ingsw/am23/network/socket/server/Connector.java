package it.polimi.ingsw.am23.network.socket.server;

import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.state.GameState;
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
        } catch (IOException e) { // communication error
            System.err.println("<Connector>: connessione chiusa –> " + e.getMessage());
        } catch (ClassNotFoundException e) {  //
            System.err.println("<Connector>: classe sconosciuta –> " + e.getMessage());
        } catch (Exception e) { // communication error
            System.err.println("<Connector>: errore –> " + e.getMessage()); // errore non gestito da dispatch
            e.printStackTrace();
        } finally {
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
                    serverController.takeExtraCard(m.getPlayerId(), m.getIndex());
                } catch (IllegalArgumentException | IllegalStateException e){
                    onActionError(ActionType.GENERIC, e.getMessage());
                }

            } else if (message instanceof DisconnectMessage m) {
                try {
                    serverController.disconnect(m.getPlayerId());
                } catch (Exception ignored){}
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
            clientSocket.close();
        } catch (IOException ignored) {}
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
    public void onGameStarted(GameState gameState) throws IOException {
        send(new OnGameStartedMessage(gameState));
    }

    @Override
    public void onGameStateChanged(GameState gameState) throws IOException {
        send(new OnGameStateChangedMessage(gameState));
    }

    @Override
    public void onEndOfPlacingPhase(GameState gameState) throws IOException {
        send(new OnEndOfPlacingPhaseMessage(gameState));
    }

    @Override
    public void onEndOfDrawingPhase(GameState gameState) throws IOException {
        send(new OnEndOfDrawingPhaseMessage(gameState));
    }

    @Override
    public void onExtraDrawRequest(GameState gameState) throws IOException {
        send(new OnExtraDrawRequestMessage(gameState));
    }

    @Override
    public void onEndOfResolvingPhase(GameState gameState) throws IOException {
        send(new OnEndOfResolvingPhaseMessage(gameState));
    }

    @Override
    public void onEraProgression(GameState gameState) throws IOException {
        send(new OnEraProgressionMessage(gameState));
    }

    @Override
    public void onGameOver(GameState gameState) throws IOException {
        send(new OnGameOverMessage(gameState));
    }

    @Override
    public void onScoreboardAvailable(GameState gameState) throws IOException {
        send(new OnScoreboardAvailableMessage(gameState));
    }

    @Override
    public void onActionError(ActionType actionType, String message) throws IOException {
        send(new OnActionErrorMessage(actionType, message));
    }

    @Override
    public void onServerCrashed() {}
}
