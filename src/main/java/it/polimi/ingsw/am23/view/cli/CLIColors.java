package it.polimi.ingsw.am23.view.cli;

import java.util.Map;

public final class CLIColors {
    // from ANSI Escape Codes
    static final String RESET = "\u001B[0m"; // cancel all effects
    static final String BOLD = "\u001B[1m";
    static final String DIM = "\u001B[2m";
    // Bright
    static final String BR_RED = "\u001B[91m";
    static final String BR_GREEN = "\u001B[92m";
    static final String BR_YELLOW = "\u001B[93m";
    static final String BR_BLUE = "\u001B[94m";
    static final String BR_MAGENTA = "\u001B[95m";
    static final String BR_CYAN = "\u001B[96m";
    static final String BR_WHITE = "\u001B[97m";
    public static final Map<String, String> TOTEM_COLORS = Map.of(
            "#EE2737", BR_RED,
            "#F1C400", BR_YELLOW,
            "#008EAA", BR_BLUE,
            "#41273B", DIM,
            "#FFFFFF", BR_WHITE
    );

    private CLIColors() {
    }

    static String totemColor(Object colorName) {
        if (colorName == null) return BR_WHITE;
        return TOTEM_COLORS.getOrDefault(colorName.toString().toUpperCase(), BR_WHITE);
    }

    static String paint(String color, String text) {
        return color + text + RESET;
    }

    static String paintBold(String color, String text) {
        return BOLD + color + text + RESET;
    }

    static String rule(int width) {
        return "-".repeat(width);
    } // create ----

    static String blankWidth(int w) {
        return " ".repeat(w);
    }
}
