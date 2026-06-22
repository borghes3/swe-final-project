package it.polimi.ingsw.am23.view.gui.controllers;

import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.view.gui.JavaFXView;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LobbyController {

    @FXML
    private ListView<String> lobbyListView;
    @FXML
    private Button joinLobbyButton;
    @FXML
    private Label errorLabel;

    private JavaFXView view;
    private List<LobbyState> lobbies = new ArrayList<>();

    public void setView(JavaFXView view) {
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

    public void showError(String reason) {
        errorLabel.setText(reason);
        errorLabel.setVisible(true);
    }

    @FXML
    private void onCreateLobbyClicked() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New Lobby");
        dialog.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();

        ChoiceBox<Integer> maxPlayersBox = new ChoiceBox<>();
        maxPlayersBox.getItems().addAll(2, 3, 4, 5);
        maxPlayersBox.setValue(5);

        //  colonna 0 = label, colonna 1 = controllo.
        //  riga 0 = nome, riga 1 = max players.
        grid.add(new Label("Lobby Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Max Players:"), 0, 1);
        grid.add(maxPlayersBox, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        nameField.textProperty().addListener((obs, old, nw) ->
                okButton.setDisable(nw.trim().isEmpty()));

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String name = nameField.getText().trim();
            int maxPlayers = maxPlayersBox.getValue();
            if (!name.isBlank()) {
                view.createLobby(name, maxPlayers);
            }
        }
    }

    @FXML
    private void onJoinLobbyClicked() {
        int index = lobbyListView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            String lobbyId = lobbies.get(index).getLobbyId();
            view.joinLobby(lobbyId);
        }
    }

    @FXML
    public void initialize() {
        joinLobbyButton.setDisable(true);
        lobbyListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> joinLobbyButton.setDisable(newVal == null)
        );
    }
}
