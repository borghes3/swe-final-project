package it.polimi.ingsw.am23.view.gui.controllers;

import it.polimi.ingsw.am23.model.payloads.LeaderboardPayload;
import it.polimi.ingsw.am23.persistence.RankingEntry;
import it.polimi.ingsw.am23.view.gui.JavaFXView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class LeaderboardController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private VBox entriesContainer;

    private JavaFXView view;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setView(JavaFXView view) {
        this.view = view;
    }

    public void showLeaderboard(LeaderboardPayload payload, String highlightNickname) {
        entriesContainer.getChildren().clear();

        if (payload == null) {
            subtitleLabel.setText("Leaderboard unavailable.");
            return;
        }

        titleLabel.setText("Global leaderboard - " + payload.playerCount() + "-player games");

        if (!payload.persistenceAvailable()) {
            subtitleLabel.setText("The server cannot reach the database.");
            return;
        }
        if (payload.entries() == null || payload.entries().isEmpty()) {
            subtitleLabel.setText("No games recorded.");
            return;
        }

        subtitleLabel.setText("Top " + payload.entries().size() + " recorded games.");

        entriesContainer.getChildren().add(buildHeader());
        for (RankingEntry e : payload.entries()) {
            boolean highlight = highlightNickname != null
                    && Objects.equals(highlightNickname, e.nickname());
            entriesContainer.getChildren().add(buildRow(e, highlight));
        }
    }

    private HBox buildHeader() {
        HBox row = new HBox(12);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new javafx.geometry.Insets(8, 16, 8, 16));
        row.setStyle("-fx-background-color: #2a1206; -fx-background-radius: 6;");

        row.getChildren().addAll(
                cell("#", 50),
                cell("Nickname", 160),
                cell("PP", 90),
                cell("Date", 140)
        );
        return row;
    }

    private HBox buildRow(RankingEntry e, boolean highlight) {
        HBox row = new HBox(12);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new javafx.geometry.Insets(6, 16, 6, 16));
        String bg = highlight ? "#b6651e" : (e.position() == 1 ? "#FFD700" : "#E8E8E8");
        String fg = highlight ? "#f5f0e8" : "#1a1a1a";
        row.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 6;");

        HBox.setHgrow(row, Priority.ALWAYS);

        row.getChildren().addAll(
                cellColored(e.position() + "°", 50, true, fg),
                cellColored(e.nickname(), 160, true, fg),
                cellColored(String.valueOf(e.score()), 90, false, fg),
                cellColored(e.matchDate() != null ? e.matchDate().format(DATE_FMT) : "-", 140, false, fg)
        );
        return row;
    }

    private Region cell(String text, double width) {
        return cellColored(text, width, true, "#f5f0e8");
    }

    private Region cellColored(String text, double width, boolean bold, String fill) {
        Label l = new Label(text != null ? text : "");
        l.setMinWidth(width);
        l.setPrefWidth(width);
        l.setStyle("-fx-text-fill: " + fill + ";" + (bold ? " -fx-font-weight: bold;" : "") + " -fx-font-size: 13;");
        return l;
    }

    @FXML
    private void onBackClicked() {
        if (view != null) view.backToScoreboard();
    }
}
