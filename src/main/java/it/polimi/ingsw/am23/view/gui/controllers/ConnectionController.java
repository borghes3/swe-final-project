package it.polimi.ingsw.am23.view.gui.controllers;

import it.polimi.ingsw.am23.view.gui.JavaFXView;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;

public class ConnectionController {

    @FXML private TextField hostField;
    @FXML private TextField nicknameField;
    @FXML private ChoiceBox<String> connectionTypeChoice;
    @FXML private Button connectButton;
    @FXML private Label errorLabel;

    private JavaFXView view;

    public void setView(JavaFXView view){ this.view = view; }

    @FXML
    public void initialize(){
        hostField.setText("localhost");
        connectionTypeChoice.getItems().addAll("RMI", "SOCKET");
        connectionTypeChoice.setValue("RMI");
        errorLabel.setText("");
    }

    @FXML
    private void onConnectClicked(){
        String host = hostField.getText().trim();
        String nickname = nicknameField.getText().trim();
        String connectionType = connectionTypeChoice.getValue();

        if(nickname.isEmpty()){
            errorLabel.setText("Insert Nickname");
            return;
        }

        if(host.isEmpty()){
            host = "localhost";
        }

        connectButton.setDisable(true);
        errorLabel.setText("Connecting...");


        final String finalHost = host;
        final String finalNickname = nickname;
        final String finalConnectionType = connectionType;

        new Thread(() -> {
            try {
                view.connect(finalHost, finalNickname, finalConnectionType);
            } catch (Exception e) {
                Platform.runLater(()-> {
                    errorLabel.setText("Error : " + e.getMessage());
                    connectButton.setDisable(false);
                });
            }
        }).start();
    }

    public void showError(String message){
        Platform.runLater(() -> {
            errorLabel.setText(message);
            connectButton.setDisable(false);
        });
    }
}
