package it.polimi.ingsw.am23.view;

import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;
import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.state.GameState;
import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.NetworkSetter;
import it.polimi.ingsw.am23.network.VirtualServer;
import it.polimi.ingsw.am23.network.VirtualView;
import it.polimi.ingsw.am23.view.controllers.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;

import java.util.List;

public class JavaFXView extends Application implements VirtualView {
    private volatile VirtualServer server;
    private volatile String playerId;
    private volatile String playerName;
    private volatile String currentLobbyId;
    private volatile boolean owner;
    private volatile boolean leftVoluntarily = false;
    private volatile GameState currentGameState;

    private Stage primaryStage;
    private ConnectionController connectionController;
    private LobbyController lobbyController;
    private WaitingRoomController waitingRoomController;
    private GameScreenController gameScreenController;
    private ScoreboardController scoreboardController;

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        primaryStage.setTitle("MESOS");
        showConnectionScreen();
        primaryStage.show();
    }

    public static void main(String[] args){
        launch(args);
    }

    // CONNECTION

    private void showConnectionScreen() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/connection.fxml")
        );
        Parent root = loader.load();
        connectionController = loader.getController();
        connectionController.setView(this);
        primaryStage.setScene(new Scene(root, 400, 500));
    }

    public void connect(String host, String nickname, String connectionType) throws Exception{
        this.playerName = nickname;
        this.server = NetworkSetter.connect(host, nickname, this, connectionType);
    }

    // LOBBY

    private void showLobbyScreen(List<LobbyState> lobbies) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/lobby.fxml")
        );
        Parent root = loader.load();
        lobbyController = loader.getController();
        lobbyController.setView(this);
        lobbyController.updateLobbies(lobbies);
        primaryStage.setScene(new Scene(root, 400, 500));
    }

    public void joinLobby(String lobbyId){
        try{
            server.joinLobby(playerId, lobbyId);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void createLobby(String name){
        try {
            server.createLobby(playerId, name, 5);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // WAITING ROOM

    private void showWaitingRoomScreen(LobbyState lobby) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/waitingRoom.fxml")
        );
        Parent root = loader.load();
        waitingRoomController = loader.getController();
        waitingRoomController.setView(this);
        waitingRoomController.setOwner(owner);
        waitingRoomController.setLobbyInfo(lobby.getLobbyId(),lobby.getLobbyName());
        waitingRoomController.updatePlayerList(lobby.getPlayers().stream().map(player -> player.getNickname()).collect(java.util.stream.Collectors.toList()));
        primaryStage.setScene(new Scene(root, 400, 500));
    }

    public void leaveLobby(){
        try{
            leftVoluntarily = true;
            server.leaveLobby(playerId, currentLobbyId);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // SCOREBOARD

    public void showScoreboardScreen(GameState gameState) throws Exception{
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/scoreboard.fxml")
        );
        Parent root = loader.load();
        scoreboardController = loader.getController();
        scoreboardController.setView(this);
        scoreboardController.showScoreboard(gameState);
        primaryStage.getScene().setRoot(root);
    }

    public void goToLobby(){
        Platform.runLater(()-> {
            try{
                scoreboardController = null;
                showLobbyScreen(java.util.List.of());
            }catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    // GAME

    private void showGameScreen(GameState gameState) throws Exception{
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/gameScreen.fxml")
        );
        Parent root = loader.load();
        gameScreenController = loader.getController();
        gameScreenController.setView(this);
        gameScreenController.setMyPlayerId(playerId);
        gameScreenController.updateGameState(gameState);
        primaryStage.setScene(new Scene(root, 1100, 800));
    }

    public void startGame() {
        try {
            server.startGame(playerId, currentLobbyId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void placeTotem(char tileId) {
        try{
            server.placeTotem(playerId, tileId);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void takeSingleCard(SelectedSingleCard card){
        try{
            server.takeSingleCard(playerId, card);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void takeExtraCard(int index){
        try{
            server.takeExtraCard(playerId, index);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // -----------------

    @Override
    public void onConnected(String playerId, List<LobbyState> lobbies) throws Exception {
        this.playerId = playerId;
        Platform.runLater(() -> {
            try {
                showLobbyScreen(lobbies);
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onConnectError(String reason) throws Exception {
        Platform.runLater(()->{
            if (connectionController != null) {
                connectionController.showError(reason);
            }
        });
    }

    @Override
    public void onLobbyListUpdated(List<LobbyState> lobbies) throws Exception {
        Platform.runLater(()-> {
            if(lobbyController != null){
                lobbyController.updateLobbies(lobbies);
            }
        });
    }

    @Override
    public void onLobbyCreated(LobbyState lobby) throws Exception {
        this.currentLobbyId = lobby.getLobbyId();
        this.owner = true;
        Platform.runLater(()-> {
            try{
                showWaitingRoomScreen(lobby);
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onLobbyUpdate(LobbyState lobby) throws Exception {
        Platform.runLater(()-> {
            try{
                // se già in waiting room aggiorna lista players
                if(waitingRoomController != null){
                    waitingRoomController.updatePlayerList(
                            lobby.getPlayers().stream()
                                    .map(player -> player.getNickname())
                                    .collect(java.util.stream.Collectors.toList())
                    );
                }else{ // chi fa join entra nella waiting room
                    this.currentLobbyId = lobby.getLobbyId();
                    this.owner = false;
                    showWaitingRoomScreen(lobby);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onJoinError(String reason) throws Exception {
        Platform.runLater(() -> {
            if(lobbyController != null){
                lobbyController.showError(reason);
            }
        });
    }

    @Override
    public void onLobbyClosed() throws Exception {
        Platform.runLater(()->{
            try {
                waitingRoomController = null;
                showLobbyScreen(java.util.List.of());
                if (!leftVoluntarily){
                    lobbyController.showError("La lobby è stata chiusa. Scegliene una nuova.");
                }
                leftVoluntarily = false;
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onGameStarted(GameState gameState) throws Exception {
        Platform.runLater(() -> {
            try{
                waitingRoomController = null;
                showGameScreen(gameState);
            } catch(Exception e){
                e.printStackTrace();
            }
        });

    }

    @Override
    public void onGameStateChanged(GameState gameState) throws Exception {
        Platform.runLater(() -> {
            if(gameScreenController != null){
                gameScreenController.updateGameState(gameState);
            }
        });
    }

    @Override
    public void onEndOfPlacingPhase(GameState gameState) throws Exception {
        Platform.runLater(() -> {
            if(gameScreenController != null){
                gameScreenController.updateGameState(gameState);
            }
        });
    }

    @Override
    public void onEndOfDrawingPhase(GameState gameState) throws Exception {
        Platform.runLater(() -> {
            if(gameScreenController != null){
                gameScreenController.updateGameState(gameState);
            }
        });
    }

    @Override
    public void onExtraDrawRequest(GameState gameState) throws Exception {
        Platform.runLater(() -> {
            if (gameScreenController != null) {
                gameScreenController.updateGameState(gameState);

                if(playerId != null && playerId.equals(gameState.getCurrentPlayerId())){
                    gameScreenController.showExtraDrawDialog(gameState);
                }
            }
        });
    }

    @Override
    public void onEndOfResolvingPhase(GameState gameState) throws Exception {
        Platform.runLater(() -> {
            if(gameScreenController != null){
                gameScreenController.updateGameState(gameState);
            }
        });
    }

    @Override
    public void onEraProgression(GameState gameState) throws Exception {
        Platform.runLater(() -> {
            if(gameScreenController != null){
                gameScreenController.updateGameState(gameState);
            }
        });
    }

    @Override
    public void onGameOver(GameState gameState) throws Exception {
        Platform.runLater(() -> {
            if(gameScreenController != null){
                gameScreenController.updateGameState(gameState);
            }
        });
    }

    @Override
    public void onScoreboardAvailable(GameState gameState) throws Exception {
        this.currentGameState = gameState;
        Platform.runLater(() -> {
            try{
                showScoreboardScreen(gameState);
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onActionError(ActionType actionType, String message) throws Exception {
        Platform.runLater( () -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.WARNING
        );
            alert.setTitle("Azione non valida");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
