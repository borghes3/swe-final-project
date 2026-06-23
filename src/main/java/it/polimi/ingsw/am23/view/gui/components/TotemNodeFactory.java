package it.polimi.ingsw.am23.view.gui.components;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Factory for graphical totem nodes used by the JavaFX board.
 * It maps logical totem colors to image resources and caches loaded images
 * so board refreshes do not reload the same files repeatedly.
 */
public final class TotemNodeFactory {

    private static final String BASE_PATH = "/images/totems/";
    private static final double TOTEM_ASPECT = 138.0 / 256.0;
    private static final Map<String, Image> CACHE = new HashMap<>();

    private TotemNodeFactory() {
    }

    /**
     * Builds a vertical totem image node.
     *
     * @param totemColor logical totem color
     * @param height desired node height
     * @return a stack pane containing the totem image
     */
    public static StackPane createVerticalTotem(String totemColor, double height) {
        ImageView imageView = new ImageView(loadImage(totemColor));
        imageView.setFitHeight(height);
        imageView.setFitWidth(height * TOTEM_ASPECT);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);

        StackPane pane = new StackPane(imageView);
        pane.setPrefSize(height * TOTEM_ASPECT, height);
        pane.setMinSize(height * TOTEM_ASPECT, height);
        pane.setMaxSize(height * TOTEM_ASPECT, height);
        pane.setMouseTransparent(true);
        return pane;
    }

    private static Image loadImage(String totemColor) {
        String fileName = fileNameFor(totemColor);
        return CACHE.computeIfAbsent(fileName, TotemNodeFactory::loadFromResources);
    }

    private static Image loadFromResources(String fileName) {
        String path = BASE_PATH + fileName;
        InputStream inputStream = TotemNodeFactory.class.getResourceAsStream(path);
        if (inputStream == null) {
            throw new IllegalArgumentException("Missing totem image: " + path);
        }
        return new Image(inputStream);
    }

    private static String fileNameFor(String totemColor) {
        return switch (normalize(totemColor)) {
            case "RED" -> "totem_red.png";
            case "YELLOW" -> "totem_yellow.png";
            case "BLUE" -> "totem_blue.png";
            case "BLACK", "PURPLE" -> "totem_black.png";
            case "WHITE" -> "totem_white.png";
            default -> "totem_white.png";
        };
    }

    private static String normalize(String totemColor) {
        if (totemColor == null || totemColor.isBlank()) {
            return "WHITE";
        }
        return totemColor.trim().toUpperCase(Locale.ROOT);
    }
}
