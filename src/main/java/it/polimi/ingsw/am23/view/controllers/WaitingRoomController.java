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

    @FXML
    private void onStartClicked(){
        // TODO : notificare che vuole avviare la partita
    }

    @FXML
    private void onLeaveClicked(){
        view.leaveLobby();
    }
}
