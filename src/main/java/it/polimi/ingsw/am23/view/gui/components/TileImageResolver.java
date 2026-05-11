package it.polimi.ingsw.am23.view.gui.components;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TileImageResolver {

    private static final String TILES_BASE_PATH = "/images/tiles/";
    private static final Map<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    private TileImageResolver() {
    }

    public static Image loadOfferTileImage(char tileId) {
        String path = TILES_BASE_PATH + "OfferTile_" + tileId + ".png";
        return IMAGE_CACHE.computeIfAbsent(path, TileImageResolver::loadImageFromResource);
    }

    public static Image loadTurnOrderImage(int playerCount) {
        String path = TILES_BASE_PATH + "TurnOrder_" + playerCount + ".png";
        return IMAGE_CACHE.computeIfAbsent(path, TileImageResolver::loadImageFromResource);
    }

    private static Image loadImageFromResource(String path) {
        InputStream inputStream = TileImageResolver.class.getResourceAsStream(path);

        if (inputStream == null) {
            throw new IllegalArgumentException("Tile image resource not found: " + path);
        }

        return new Image(inputStream);
    }
}