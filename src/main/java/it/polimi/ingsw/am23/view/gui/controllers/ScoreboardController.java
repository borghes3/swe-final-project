package it.polimi.ingsw.am23.view.gui.controllers;

import it.polimi.ingsw.am23.model.payloads.MatchRankingsPayload;
import it.polimi.ingsw.am23.model.payloads.PlayerScore;
import it.polimi.ingsw.am23.model.payloads.ScoreBoardPayload;
import it.polimi.ingsw.am23.view.gui.JavaFXView;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;

public class ScoreboardController {
    @FXML
    private VBox rootContainer;
    @FXML
    private Label titleLabel;
    @FXML
    private VBox rankingContainer;
    @FXML
    private Label positionLabel;
    @FXML
    private Button leaderboardButton;
    @FXML
    private Button menuButton;

    private JavaFXView view;
    private int matchPlayerCount = -1;

    public void setView(JavaFXView view) {
        this.view = view;
    }

    public Parent getRoot() {
        return rootContainer;
    }

    public void showScoreboard(ScoreBoardPayload payload) {
        if (payload == null || payload.scores() == null) return;

        matchPlayerCount = payload.scores().size();

        List<PlayerScore> sorted = payload.scores().stream()
                .sorted(Comparator.comparingInt(PlayerScore::totalPrestigePoints)
                        .thenComparingInt(PlayerScore::foodPoints)
                        .reversed())
                .toList();

        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            if (i == 0) {
                positions.add(1);
            } else {
                PlayerScore prev = sorted.get(i - 1);
                PlayerScore curr = sorted.get(i);
                if (curr.totalPrestigePoints() == prev.totalPrestigePoints()
                        && curr.foodPoints() == prev.foodPoints()) {
                    positions.add(positions.get(i - 1));
                } else {
                    positions.add(i + 1);
                }
            }
        }

        rankingContainer.getChildren().clear();
        rankingContainer.setSpacing(10);

        for (int i = 0; i < sorted.size(); i++) {
            PlayerScore entry = sorted.get(i);
            int pos = positions.get(i);
            String nickname = entry.nickname() != null ? entry.nickname() : entry.playerId();
            HBox row = buildRow(pos, nickname, entry.totalPrestigePoints(), entry.foodPoints());
            rankingContainer.getChildren().add(row);
        }

        if (positionLabel != null) {
            positionLabel.setText("Classifica globale in arrivo…");
        }
        if (leaderboardButton != null) {
            leaderboardButton.setDisable(true);
        }
    }

    public void showMatchRankings(String myPlayerId, MatchRankingsPayload payload) {
        if (payload == null || positionLabel == null) return;

        if (!payload.persistenceAvailable()) {
            positionLabel.setText("Classifica non disponibile (DB offline).");
            if (leaderboardButton != null) {
                leaderboardButton.setDisable(true);
            }
            return;
        }

        Integer pos = payload.positionByPlayerId() != null
                ? payload.positionByPlayerId().get(myPlayerId)
                : null;

        if (pos == null || pos <= 0) {
            positionLabel.setText("Posizione globale: n/d (partite a "
                    + payload.playerCount() + " giocatori)");
        } else {
            positionLabel.setText("Sei #" + pos + " nella classifica globale delle partite a "
                    + payload.playerCount() + " giocatori.");
        }

        if (leaderboardButton != null) {
            leaderboardButton.setDisable(false);
        }
    }

    private HBox buildRow(int position, String nickname, int pp, int food) {
        HBox row = new HBox(16);
        row.setAlignment(javafx.geometry.Pos.CENTER);
        row.setPadding(new javafx.geometry.Insets(10, 20, 10, 20));
        row.setPrefWidth(360);

        String bgColor = position == 1 ? "#FFD700" : "#E8E8E8";
        String textColor = "#1a1a1a";

        row.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
        );

        String medal = position == 1 ? "🥇" : "°";

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
    private void onLeaderboardClicked() {
        if (view != null && matchPlayerCount > 0) {
            view.requestLeaderboard(matchPlayerCount);
        }
    }

    @FXML
    private void onMenuClicked() {
        view.goToLobby();
    }

}
