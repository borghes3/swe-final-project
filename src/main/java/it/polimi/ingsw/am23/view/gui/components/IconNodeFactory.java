package it.polimi.ingsw.am23.view.gui.components;

import it.polimi.ingsw.am23.model.enums.CharacterType;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public final class IconNodeFactory {

    public static final String FOOD_ICON = "food.png";
    public static final String PRESTIGE_ICON = "PP.png";

    private IconNodeFactory() {
    }

    public static ImageView createIcon(String fileName, double size) {
        ImageView imageView = new ImageView(IconImageResolver.loadIcon(fileName));
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        return imageView;
    }

    public static HBox createIconWithText(String fileName, String text, double iconSize) {
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER_LEFT);

        ImageView icon = createIcon(fileName, iconSize);

        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");

        row.getChildren().addAll(icon, label);
        return row;
    }

    public static HBox createSmallIconWithText(String fileName, String text, double iconSize) {
        HBox row = new HBox(3);
        row.setAlignment(Pos.CENTER_LEFT);

        ImageView icon = createIcon(fileName, iconSize);

        Label label = new Label(text);
        label.setStyle("-fx-text-fill: rgba(245,240,232,0.75); -fx-font-size: 10px;");

        row.getChildren().addAll(icon, label);
        return row;
    }

    public static ImageView createCharacterTypeIcon(CharacterType characterType, double size) {
        return createIcon(iconFileForCharacterType(characterType), size);
    }

    public static String iconFileForCharacterType(CharacterType characterType) {
        return switch (characterType) {
            case ARTIST -> "IA.png";
            case BUILDER -> "IB.png";
            case GATHERER -> "IG.png";
            case HUNTER -> "IH.png";
            case INVENTOR -> "II.png";
            case SHAMAN -> "IS.png";
        };
    }
}
