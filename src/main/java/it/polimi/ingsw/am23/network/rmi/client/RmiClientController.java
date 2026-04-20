package it.polimi.ingsw.am23.network.rmi.client;

import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.state.GameState;
import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.VirtualView;
import it.polimi.ingsw.am23.view.tui.LanternaTuiView;

import java.util.List;
import java.util.Objects;

/**
 * Client controller per RMI
 * riceve azioni dalla view e chiama metodi sul remote server
 */
public final class RmiClientController implements VirtualView {

    private static final int DEFAULT_LOBBY_MAX_PLAYERS = 99;

    private final LanternaTuiView view;
    private VirtualServerRmi server;

    public RmiClientController(LanternaTuiView view) {
        this.view = Objects.requireNonNull(view, "view cannot be null");
    }

    public void connect(String host, String nickname) throws Exception {
        this.server = RmiClient.connect(host, nickname, this);
        view.awaitConnected();
    }

    public void refreshLobby() {
        view.requestLobbyRefresh();
    }

    public void createLobby(String lobbyName) throws Exception {
        server.createLobby(view.getPlayerId(), lobbyName, DEFAULT_LOBBY_MAX_PLAYERS);
    }

    public void joinLobby(String lobbyCode) throws Exception {
        server.joinLobby(view.getPlayerId(), lobbyCode);
    }

    public void startGame() throws Exception {
        server.startGame(view.getPlayerId(), view.getCurrentLobbyId());
    }

    public boolean isGameStarted() {
        return view.isGameStarted();
    }

    public boolean isOwner() {
        return view.isOwner();
    }

    public String getCurrentLobbyId() {
        return view.getCurrentLobbyId();
    }

    public void awaitGameStarted() throws InterruptedException {
        view.awaitGameStarted();
    }

    @Override
    public void onConnected(String playerId, List<LobbyState> lobbies) throws Exception {
        view.onConnected(playerId, lobbies);
    }

    @Override
    public void onConnectError(String reason) throws Exception {
        view.onConnectError(reason);
    }

    @Override
    public void onLobbyListUpdated(List<LobbyState> lobbies) throws Exception {
        view.onLobbyListUpdated(lobbies);
    }

    @Override
    public void onLobbyCreated(LobbyState lobby) throws Exception {
        view.onLobbyCreated(lobby);
    }

    @Override
    public void onLobbyUpdate(LobbyState lobby) throws Exception {
        view.onLobbyUpdate(lobby);
    }

    @Override
    public void onJoinError(String reason) throws Exception {
        view.onJoinError(reason);
    }

    @Override
    public void onLobbyClosed() throws Exception {
        view.onLobbyClosed();
    }

    @Override
    public void onGameStarted(GameState gameState) throws Exception {
        view.onGameStarted(gameState);
    }

    @Override
    public void onGameStateChanged(GameState gameState) throws Exception {
        view.onGameStateChanged(gameState);
    }

    @Override
    public void onEndOfPlacingPhase(GameState gameState) throws Exception {
        view.onEndOfPlacingPhase(gameState);
    }

    @Override
    public void onEndOfDrawingPhase(GameState gameState) throws Exception {
        view.onEndOfDrawingPhase(gameState);
    }

    @Override
    public void onExtraDrawRequest(GameState gameState) throws Exception {
        view.onExtraDrawRequest(gameState);
    }

    @Override
    public void onEndOfResolvingPhase(GameState gameState) throws Exception {
        view.onEndOfResolvingPhase(gameState);
    }

    @Override
    public void onEraProgression(GameState gameState) throws Exception {
        view.onEraProgression(gameState);
    }

    @Override
    public void onGameOver() throws Exception {
        view.onGameOver();
    }

    @Override
    public void onScoreboardAvailable() throws Exception {
        view.onScoreboardAvailable();
    }

    @Override
    public void onActionError(ActionType actionType, String message) throws Exception {
        view.onActionError(actionType, message);
    }
}
