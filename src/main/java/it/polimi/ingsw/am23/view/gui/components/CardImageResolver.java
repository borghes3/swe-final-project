package it.polimi.ingsw.am23.view.gui.components;

import it.polimi.ingsw.am23.model.enums.CardKind;
import it.polimi.ingsw.am23.model.state.CardState;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class CardImageResolver {

    private static final String CARDS_BASE_PATH = "/images/cards/";

    private static final Map<String, String> ID_TO_IMAGE_NAME = createIdToImageNameMap();
    private static final Map<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    private CardImageResolver() {
    }

    public static String resolveImagePath(CardState cardState) {
        Objects.requireNonNull(cardState, "cardState cannot be null");

        if (cardState.getCardKind() == CardKind.BUILDING) {
            return CARDS_BASE_PATH + cardState.getCardId() + ".png";
        }

        String imageName = ID_TO_IMAGE_NAME.get(cardState.getCardId());
        if (imageName == null) {
            throw new IllegalArgumentException("No image mapping found for card id: " + cardState.getCardId());
        }

        return CARDS_BASE_PATH + imageName + ".png";
    }

    public static Image loadImage(CardState cardState) {
        String path = resolveImagePath(cardState);
        return IMAGE_CACHE.computeIfAbsent(path, CardImageResolver::loadImageFromResource);
    }

    public static Map<String, String> getIdToImageNameMap() {
        return ID_TO_IMAGE_NAME;
    }

    private static Image loadImageFromResource(String path) {
        InputStream inputStream = CardImageResolver.class.getResourceAsStream(path);

        if (inputStream == null) {
            throw new IllegalArgumentException("Image resource not found: " + path);
        }

        return new Image(inputStream);
    }

    public static Image loadCharacterBackImage(int eraNumber) {
        int safeEra = Math.max(1, Math.min(3, eraNumber));
        String path = CARDS_BASE_PATH + "C_back_" + safeEra + ".png";
        return IMAGE_CACHE.computeIfAbsent(path, CardImageResolver::loadImageFromResource);
    }

    private static Map<String, String> createIdToImageNameMap() {
        Map<String, String> map = new HashMap<>();

        map.put("ECP00", "ECP_1");
        map.put("ECP01", "ECP_2");
        map.put("ECP02", "ECP_3");
        map.put("EHU03", "EHU_1");
        map.put("EHU04", "EHU_2");
        map.put("EHU05", "EHU_3");
        map.put("ESH06", "ESH_1");
        map.put("ESH07", "ESH_2");
        map.put("ESH08", "ESH_3");
        map.put("ESU09", "ESU_1");
        map.put("ESU10", "ESU_2");
        map.put("ESU11", "ESU_3");

        map.put("CA00", "CA");
        map.put("CA01", "CA");
        map.put("CA02", "CA");
        map.put("CA03", "CA");
        map.put("CA04", "CA");
        map.put("CA05", "CA");
        map.put("CA06", "CA");
        map.put("CA07", "CA");
        map.put("CA08", "CA");
        map.put("CA09", "CA");
        map.put("CA10", "CA");
        map.put("CA11", "CA");
        map.put("CA12", "CA");

        map.put("CB13", "CB_0_2");
        map.put("CB14", "CB_2_1");
        map.put("CB15", "CB_3_1");
        map.put("CB16", "CB_3_2");
        map.put("CB17", "CB_4_1");
        map.put("CB18", "CB_1_2");
        map.put("CB19", "CB_5_1");
        map.put("CB20", "CB_3_2");
        map.put("CB21", "CB_2_2");
        map.put("CB22", "CB_2_1");
        map.put("CB23", "CB_1_2");
        map.put("CB24", "CB_4_1");

        map.put("CG25", "CG");
        map.put("CG26", "CG");
        map.put("CG27", "CG");
        map.put("CG28", "CG");
        map.put("CG29", "CG");
        map.put("CG30", "CG");
        map.put("CG31", "CG");
        map.put("CG32", "CG");
        map.put("CG33", "CG");
        map.put("CG34", "CG");
        map.put("CG35", "CG");

        map.put("CH36", "CH_senza");
        map.put("CH37", "CH_con");
        map.put("CH38", "CH_con");
        map.put("CH39", "CH_senza");
        map.put("CH40", "CH_senza");
        map.put("CH41", "CH_con");
        map.put("CH42", "CH_senza");
        map.put("CH43", "CH_senza");
        map.put("CH44", "CH_con");
        map.put("CH45", "CH_senza");
        map.put("CH46", "CH_senza");
        map.put("CH47", "CH_con");
        map.put("CH48", "CH_con");
        map.put("CH49", "CH_senza");
        map.put("CH50", "CH_con");

        map.put("CI51", "CI_Arrow");
        map.put("CI52", "CI_Boat");
        map.put("CI53", "CI_Bread");
        map.put("CI54", "CI_Patch");
        map.put("CI55", "CI_Bowl");
        map.put("CI56", "CI_Flute");
        map.put("CI57", "CI_Patch");
        map.put("CI58", "CI_Rope");
        map.put("CI59", "CI_Statue");
        map.put("CI60", "CI_Bread");
        map.put("CI61", "CI_Hook");
        map.put("CI62", "CI_Neck");
        map.put("CI63", "CI_Statue");
        map.put("CI64", "CI_Arrow");
        map.put("CI65", "CI_Boat");
        map.put("CI66", "CI_Bowl");
        map.put("CI67", "CI_Flute");
        map.put("CI68", "CI_Rope");
        map.put("CI69", "CI_Hook");
        map.put("CI70", "CI_Neck");

        map.put("CS71", "CS_1");
        map.put("CS72", "CS_2");
        map.put("CS73", "CS_2");
        map.put("CS74", "CS_2");
        map.put("CS75", "CS_2");
        map.put("CS76", "CS_3");
        map.put("CS77", "CS_3");
        map.put("CS78", "CS_2");
        map.put("CS79", "CS_1");
        map.put("CS80", "CS_2");
        map.put("CS81", "CS_2");
        map.put("CS82", "CS_1");
        map.put("CS83", "CS_2");

        return Collections.unmodifiableMap(map);
    }
}