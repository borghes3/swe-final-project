package it.polimi.ingsw.am23.view.gui.components;

import javafx.scene.image.Image;

import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class IconImageResolver {

    private static final String ICONS_BASE_PATH = "/images/icons/";
    private static final Map<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    private IconImageResolver() {
    }

    public static Image loadIcon(String fileName) {
        Objects.requireNonNull(fileName, "fileName cannot be null");

        return IMAGE_CACHE.computeIfAbsent(fileName, IconImageResolver::loadIconFromResource);
    }

    private static Image loadIconFromResource(String fileName) {
        String path = ICONS_BASE_PATH + fileName;
        URL resource = IconImageResolver.class.getResource(path);

        if (resource == null) {
            throw new IllegalArgumentException("Icon resource not found: " + path);
        }

        return new Image(resource.toExternalForm());
    }
}
