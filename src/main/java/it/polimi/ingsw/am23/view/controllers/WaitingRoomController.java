package it.polimi.ingsw.am23.view.controllers;

import it.polimi.ingsw.am23.view.JavaFXView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;

import java.util.List;

public class WaitingRoomController {
    @FXML private ListView<String> lobbyListView;
    @FXML private Button startButton;
    @FXML private Button leaveButton;

    private JavaFXView view;

    public void setView(JavaFXView view){
        this.view = view;
    }

    public void setOwner(boolean isOwner){
        startButton.setVisible(isOwner);
    }

    public void updatePlayerList(List<String> players){
        Platform.runLater(()->{
            lobbyListView.getItems().setAll(players);
        });
    }

    public void showError(String message){
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onStartClicked(){
        if(lobbyListView.getItems().size() < 2){
            showError("Servono almeno 2 giocatori per iniziare la partita.");
            return;
        }
        view.startGame();
    }

    @FXML
    private void onLeaveClicked(){
        view.leaveLobby();
    }
}
