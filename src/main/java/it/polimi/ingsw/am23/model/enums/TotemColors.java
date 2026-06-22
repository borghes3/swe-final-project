package it.polimi.ingsw.am23.model.enums;

/**
 * Color identity of a player's totem. Each value exposes its canonical hex
 * color via {@link #getColor()} for use by the GUI renderer.
 */
public enum TotemColors {
    /**
     * Red totem.
     */
    RED,
    /**
     * Yellow totem.
     */
    YELLOW,
    /**
     * Blue totem.
     */
    BLUE,
    /**
     * Black totem.
     */
    BLACK,
    /**
     * White totem.
     */
    WHITE;

    /**
     * Returns the hex color string associated with this totem color.
     *
     * @return a {@code #RRGGBB} hex color string
     */
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
