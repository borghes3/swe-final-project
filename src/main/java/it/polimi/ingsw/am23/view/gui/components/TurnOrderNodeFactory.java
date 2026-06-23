package it.polimi.ingsw.am23.view.gui.components;

import it.polimi.ingsw.am23.model.state.TurnOrderSlotState;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.List;
import java.util.Map;

public final class TurnOrderNodeFactory {

    private TurnOrderNodeFactory() {
    }

    public static StackPane createTurnOrderNode(List<TurnOrderSlotState> slots,
                                                Map<String, String> nicknames,
                                                Map<String, String> totemColors,
                                                double width,
                                                double height) {
        int playerCount = slots.size();

        double borderPadding = 2.0;
        double imageWidth = width - 2 * borderPadding;
        double imageHeight = height - 2 * borderPadding;

        StackPane root = new StackPane();
        root.setPrefSize(width, height);
        root.setMinSize(width, height);
        root.setMaxSize(width, height);
        root.setAlignment(Pos.CENTER);
        root.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: rgba(245,240,232,0.85);" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-padding: " + borderPadding + ";"
        );

        try {
            Image image = TileImageResolver.loadTurnOrderImage(playerCount);

            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(imageWidth);
            imageView.setFitHeight(imageHeight);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setCache(true);

            Rectangle clip = new Rectangle(imageWidth, imageHeight);
            clip.setArcWidth(14);
            clip.setArcHeight(14);
            imageView.setClip(clip);

            root.getChildren().add(imageView);
        } catch (RuntimeException e) {
            Label fallback = new Label("Turn order\n" + e.getMessage());
            fallback.setWrapText(true);
            fallback.setAlignment(Pos.CENTER);
            fallback.setStyle("-fx-text-fill: white; -fx-font-size: 9px;");
            root.getChildren().add(fallback);
        }

        Pane overlay = new Pane();
        overlay.setPrefSize(width, height);
        overlay.setMinSize(width, height);
        overlay.setMaxSize(width, height);

        double markerHeight = Math.max(28, Math.min(46, height * 0.27));
        double markerWidth = markerHeight * 138.0 / 256.0;

        for (TurnOrderSlotState slot : slots) {
            String playerId = slot.occupiedByPlayerId();
            if (playerId == null) {
                continue;
            }

            StackPane marker = TotemNodeFactory.createVerticalTotem(
                    totemColors.getOrDefault(playerId, "WHITE"),
                    markerHeight
            );

            double[] coordinates = slotCoordinates(
                    playerCount,
                    slot.positionIndex(),
                    width,
                    height,
                    markerWidth,
                    markerHeight
            );

            marker.setLayoutX(coordinates[0]);
            marker.setLayoutY(coordinates[1]);

            overlay.getChildren().add(marker);
        }

        root.getChildren().add(overlay);

        return root;
    }

    private static StackPane buildPlayerMarker(String nickname, String color, double size) {
        StackPane marker = new StackPane();
        marker.setPrefSize(size, size);
        marker.setMinSize(size, size);
        marker.setMaxSize(size, size);

        Circle circle = new Circle(size / 2.0);
        circle.setStyle(
                "-fx-fill: " + color + ";" +
                        "-fx-stroke: rgba(0,0,0,0.75);" +
                        "-fx-stroke-width: 1.5;"
        );

        Label label = new Label(shortName(nickname));
        label.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: " + Math.max(6, size * 0.45) + "px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-effect: dropshadow(gaussian, black, 2, 0.8, 0, 0);"
        );

        marker.getChildren().addAll(circle, label);
        return marker;
    }

    private static String shortName(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return "?";
        }

        String trimmed = nickname.trim();
        return trimmed.length() <= 2 ? trimmed.toUpperCase() : trimmed.substring(0, 2).toUpperCase();
    }

    private static double[] slotCoordinates(int playerCount,
                                            int positionIndex,
                                            double width,
                                            double height,
                                            double markerWidth,
                                            double markerHeight) {
        double[][] normalized = switch (playerCount) {
            case 2 -> new double[][]{
                    {0.50, 0.24},
                    {0.50, 0.40}
            };
            case 3 -> new double[][]{
                    {0.50, 0.19},
                    {0.50, 0.365},
                    {0.50, 0.54}
            };
            case 4 -> new double[][]{
                    {0.50, 0.14},
                    {0.50, 0.32},
                    {0.50, 0.50},
                    {0.50, 0.68}
            };
            case 5 -> new double[][]{
                    {0.50, 0.08},
                    {0.50, 0.25},
                    {0.50, 0.43},
                    {0.50, 0.61},
                    {0.50, 0.78}
            };
            default -> throw new IllegalArgumentException("Unsupported turn order slot count: " + playerCount);
        };

        int safeIndex = Math.max(0, Math.min(positionIndex, normalized.length - 1));

        double x = normalized[safeIndex][0] * width - markerWidth / 2.0;
        double y = normalized[safeIndex][1] * height - markerHeight / 2.0;

        return new double[]{x, y};
    }
}