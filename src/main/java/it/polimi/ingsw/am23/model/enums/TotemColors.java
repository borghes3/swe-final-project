package it.polimi.ingsw.am23.model.enums;

public enum TotemColors {
    RED, YELLOW, BLUE, BLACK, WHITE;

    public String getColor() {
        return switch (this) {
            case RED -> "#EE2737";
            case YELLOW -> "#F1C400";
            case BLUE -> "#008EAA";
            case BLACK -> "#41273B";
            case WHITE -> "#FFFFFF";
        };
    }
}
