package it.polimi.ingsw.am23.view.controllers;

import it.polimi.ingsw.am23.model.state.GameState;
import it.polimi.ingsw.am23.model.state.PlayerState;
import it.polimi.ingsw.am23.model.state.ScoreEntry;
import it.polimi.ingsw.am23.view.JavaFXView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.stream.Collectors;


public class ScoreboardController {
    @FXML private Label titleLabel;
    @FXML private VBox rankingContainer;
    @FXML private Button menuButton;

    private JavaFXView view;

    public void setView(JavaFXView view){
        this.view = view;
    }

    public void showScoreboard(GameState gameState){

        /* TEST scoreboard - rimuovi dopo
        if (gameState == null) {
            rankingContainer.getChildren().clear();
            rankingContainer.setSpacing(10);
            rankingContainer.getChildren().add(buildRow(1, "Mario", 12, 8));
            rankingContainer.getChildren().add(buildRow(2, "Luigi", 10, 8));
            rankingContainer.getChildren().add(buildRow(3, "Peach", 9, 5));
            rankingContainer.getChildren().add(buildRow(4, "Peaches", 8, 5));
            rankingContainer.getChildren().add(buildRow(5, "Pe", 7, 5));
            return;
        }*/

        List<ScoreEntry> scores = gameState.getScores();
        if(scores == null)
            return;

        Map<String, String> nicknames = gameState.getPlayers().stream()
                .collect(Collectors.toMap(PlayerState::getPlayerId, PlayerState::getNickname));

        // ordina per PP decrescenti e usa food come tiebreaker
        List<ScoreEntry> sorted = scores.stream()
                .sorted(Comparator.comparingInt(ScoreEntry::prestigePoints)
                    .thenComparingInt(ScoreEntry::foodPoints)
                    .reversed())
                .collect(Collectors.toList());

        // calcolo posizioni
        List<Integer> positions = new ArrayList<>();
        for(int i=0; i<sorted.size(); i++){
            if(i==0){
                positions.add(1);
            }else{
                ScoreEntry prev = sorted.get(i-1);
                ScoreEntry curr = sorted.get(i);
                if(curr.prestigePoints() == prev.prestigePoints()
                        && curr.foodPoints() == prev.foodPoints()){
                    positions.add(positions.get(i-1)); // stessa posizione del precedente
                }else{
                    positions.add(i+1); // per saltare le posizioni
                }
            }
        }

        // costruzione righe
        rankingContainer.getChildren().clear();
        rankingContainer.setSpacing(10);

        for(int i=0; i<sorted.size(); i++){
            ScoreEntry entry = sorted.get(i);
            int pos = positions.get(i);
            String nickname = nicknames.getOrDefault(entry.playerId(), entry.playerId());

            HBox row = buildRow(pos, nickname, entry.prestigePoints(), entry.foodPoints());
            rankingContainer.getChildren().add(row);
        }
    }

    private HBox buildRow(int position, String nickname, int pp, int food){
        HBox row = new HBox(16);
        row.setAlignment(javafx.geometry.Pos.CENTER);
        row.setPadding(new javafx.geometry.Insets(10, 20, 10, 20));
        row.setPrefWidth(360);

        String bgColor = switch(position){
            case 1 -> "#FFD700"; // oro
            default -> "#E8E8E8"; // grigio chiaro
        };
        String textColor = "#1a1a1a";

        row.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-background-radius: 8;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
        );

        String medal = switch (position){
            case 1 -> "🥇";
            default -> position + "°";
        };

        Label positionLabel = new Label(medal);
        positionLabel.setStyle("-fx-font-size: 20; -fx-min-width: 40;");

        Label nameLabel = new Label(nickname);
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: " + textColor + "; -fx-min-width: 120;");

        Label ppLabel = new Label("⭐ " + pp + " PP");
        ppLabel.setStyle("-fx-font-size: 14; -fx-text-fill: " + textColor + "; -fx-min-width: 80;");

        Label foodLabel = new Label("🍖 " + food);
        foodLabel.setStyle("-fx-font-size: 14; -fx-text-fill: " + textColor + ";");

        row.getChildren().addAll(positionLabel, nameLabel, ppLabel, foodLabel);
        return row;
    }

    @FXML
    private void onMenuClicked(){
        view.goToLobby();
    }
}
