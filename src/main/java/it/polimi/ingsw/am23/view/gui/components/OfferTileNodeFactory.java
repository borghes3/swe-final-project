package it.polimi.ingsw.am23.view.gui.components;

import it.polimi.ingsw.am23.model.state.OfferTileState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public final class OfferTileNodeFactory {

    private OfferTileNodeFactory() {
    }

    public static StackPane createOfferTileNode(OfferTileState tile,
                                                double width,
                                                double height,
                                                String borderColor,
                                                String occupantName) {
        return createOfferTileNode(tile, width, height, borderColor, occupantName, "WHITE");
    }

    /**
     * Builds an offer tile node and, when occupied, places the player's
     * graphical totem on the tile.
     *
     * @param tile tile state to render
     * @param width node width
     * @param height node height
     * @param borderColor CSS color used for the tile border
     * @param occupantName nickname of the occupying player, or {@code null}
     * @param occupantTotemColor logical color used to choose the totem image
     * @return JavaFX node representing the offer tile
     */
    public static StackPane createOfferTileNode(OfferTileState tile,
                                                double width,
                                                double height,
                                                String borderColor,
                                                String occupantName,
                                                String occupantTotemColor) {
        StackPane root = new StackPane();
        root.setPrefSize(width, height);
        root.setMinSize(width, height);
        root.setMaxSize(width, height);
        root.setAlignment(Pos.CENTER);

        try {
            Image image = TileImageResolver.loadOfferTileImage(tile.tileId());

            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setCache(true);

            Rectangle clip = new Rectangle(width, height);
            clip.setArcWidth(14);
            clip.setArcHeight(14);
            imageView.setClip(clip);

            root.getChildren().add(imageView);
        } catch (RuntimeException e) {
            root.getChildren().add(buildFallback(tile, e.getMessage()));
        }

        if (occupantName != null) {
            StackPane totem = buildTopSlotTotem(occupantTotemColor, width, height);
            root.getChildren().add(totem);
        }

        root.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 2;" +
                        "-fx-padding: 0;"
        );

        return root;
    }

    private static VBox buildOccupantOverlay(String occupantName, String color) {
        VBox overlay = new VBox(2);
        overlay.setAlignment(Pos.CENTER);
        overlay.setMaxWidth(Double.MAX_VALUE);
        overlay.setStyle(
                "-fx-background-color: rgba(0,0,0,0.55);" +
                        "-fx-background-radius: 0 0 8 8;" +
                        "-fx-padding: 2 4 2 4;"
        );

        Label label = new Label(occupantName);
        label.setStyle(
                "-fx-text-fill: " + color + ";" +
                        "-fx-font-size: 8px;" +
                        "-fx-font-weight: bold;"
        );

        overlay.getChildren().add(label);
        return overlay;
    }

    /**
     * Creates the totem marker shown on the upper slot of an occupied offer tile.
     *
     * @param totemColor logical totem color
     * @param tileWidth rendered tile width
     * @param tileHeight rendered tile height
     * @return totem node positioned by the caller's stack pane constraints
     */
    private static StackPane buildTopSlotTotem(String totemColor, double tileWidth, double tileHeight) {
        double totemHeight = Math.max(24, Math.min(38, tileHeight * 0.23));
        StackPane totem = TotemNodeFactory.createVerticalTotem(totemColor, totemHeight);
        totem.setMouseTransparent(true);

        StackPane.setAlignment(totem, Pos.TOP_CENTER);
        StackPane.setMargin(totem, new Insets(tileHeight * 0.08, 0, 0, 0));

        return totem;
    }

    private static Label buildFallback(OfferTileState tile, String reason) {
        Label label = new Label("Tile " + tile.tileId() + "\n" + reason);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 8px; -fx-padding: 4;");
        return label;
    }
}
