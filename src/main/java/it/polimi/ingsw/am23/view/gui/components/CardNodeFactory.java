package it.polimi.ingsw.am23.view.gui.components;

import it.polimi.ingsw.am23.model.enums.CardKind;
import it.polimi.ingsw.am23.model.state.CardState;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Objects;

public final class CardNodeFactory {

    private static final double ARC = 10.0;

    private CardNodeFactory() {
    }

    public static VBox createCardNode(CardState cardState, double width, double height) {
        Objects.requireNonNull(cardState, "cardState cannot be null");

        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.setPrefSize(width, height);
        box.setMinSize(width, height);
        box.setMaxSize(width, height);
        box.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        StackPane cardPane = new StackPane();
        cardPane.setAlignment(Pos.CENTER);
        cardPane.setPrefSize(width, height);
        cardPane.setMinSize(width, height);
        cardPane.setMaxSize(width, height);

        try {
            Image image = CardImageResolver.loadImage(cardState);

            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);

            /*
             * Since cardW/cardH are already computed with the card aspect ratio,
             * forcing the image to fill the box avoids small empty gaps.
             */
            imageView.setPreserveRatio(false);
            imageView.setSmooth(true);
            imageView.setCache(true);

            Rectangle clip = new Rectangle(width, height);
            clip.setArcWidth(ARC);
            clip.setArcHeight(ARC);
            imageView.setClip(clip);

            cardPane.getChildren().add(imageView);

            if (cardState.getCardKind() == CardKind.BUILDING) {
                cardPane.getChildren().add(buildBuildingBorder(width, height));
            }

            box.getChildren().add(cardPane);

        } catch (RuntimeException e) {
            box.getChildren().add(buildFallbackLabel(cardState, e.getMessage()));
        }

        return box;
    }

    private static Rectangle buildBuildingBorder(double width, double height) {
        double strokeWidth = 2.0;

        Rectangle border = new Rectangle(width - strokeWidth, height - strokeWidth);
        border.setArcWidth(ARC);
        border.setArcHeight(ARC);
        border.setFill(Color.TRANSPARENT);
        border.setStroke(Color.web("#f1c400"));
        border.setStrokeWidth(strokeWidth);
        border.setMouseTransparent(true);

        return border;
    }

    private static Label buildFallbackLabel(CardState cardState, String reason) {
        Label label = new Label(cardState.getCardId() + "\n" + reason);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 8px; -fx-padding: 4;");
        return label;
    }
}