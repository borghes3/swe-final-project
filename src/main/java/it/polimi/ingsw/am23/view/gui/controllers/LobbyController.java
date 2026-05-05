package it.polimi.ingsw.am23.view.gui.controllers;

import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.view.gui.JavaFXView;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LobbyController {

    @FXML private ListView<String> lobbyListView;
    @FXML private Button joinLobbyButton;
    @FXML private Label errorLabel;

    private JavaFXView view;
    private List<LobbyState> lobbies = new ArrayList<>();

    public void setView(JavaFXView view){
        this.view = view;
    }

    public void updateLobbies(List<LobbyState> lobbies) {
        this.lobbies = new ArrayList<>(lobbies);
        lobbyListView.getItems().clear();
        for (LobbyState lobby : lobbies) {
            lobbyListView.getItems().add(
                    "[" + lobby.getLobbyId() + "] " +
                    lobby.getLobbyName() + " | " +
                    lobby.getCurrentPlayers() + "/" +
                    lobby.getMaxPlayers()
            );
        }
    }

    public void showError(String reason){
        errorLabel.setText(reason);
        errorLabel.setVisible(true);
    }

    @FXML
    private void onCreateLobbyClicked(){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Lobby");
        dialog.setHeaderText(null);
        dialog.setContentText("Lobby Name :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if(!name.isBlank()){
                view.createLobby(name);
            }
        });
    }

    @FXML
    private void onJoinLobbyClicked(){
        int index = lobbyListView.getSelectionModel().getSelectedIndex();
        if(index >= 0){
            String lobbyId = lobbies.get(index).getLobbyId();
            view.joinLobby(lobbyId);
        }
    }

    @FXML
    public void initialize(){
        joinLobbyButton.setDisable(true);
        lobbyListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> joinLobbyButton.setDisable(newVal == null)
        );
    }
}
