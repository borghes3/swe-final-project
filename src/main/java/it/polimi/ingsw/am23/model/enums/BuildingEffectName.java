package it.polimi.ingsw.am23.model.enums;

/**
 * Canonical identifier of each building effect implementation.
 * Used for serialization and to bind effect data loaded from JSON to the
 * corresponding {@link it.polimi.ingsw.am23.model.effects.BuildingEffect}.
 */
public enum BuildingEffectName {
    /**
     * Grants extra food per Artist character owned.
     */
    CAVE_PAINTINGS_FOOD_PER_ARTIST_EFFECT,
    /**
     * Doubles the end-game contribution of Builder cards.
     */
    DOUBLE_BUILDER_END_GAME_EFFECT,
    /**
     * Doubles the points granted by a Shaman ritual win.
     */
    DOUBLE_SHAMAN_WIN_EFFECT,
    /**
     * Awards end-game points per character type collected.
     */
    END_GAME_POINTS_PER_CHARACTER_TYPE_EFFECT,
    /**
     * Awards end-game points per completed character set.
     */
    END_GAME_POINTS_PER_COMPLETE_SET_EFFECT,
    /**
     * Allows the player to draw an additional card during the draw phase.
     */
    EXTRA_DRAW_EFFECT,
    /**
     * Awards a fixed amount of end-game points.
     */
    FLAT_END_GAME_POINTS_EFFECT,
    /**
     * Grants extra food when on certain turn order slots.
     */
    FOOD_FROM_TURN_ORDER_BONUS_EFFECT,
    /**
     * Awards food per completed character set.
     */
    FOOD_PER_COMPLETE_SET_EFFECT,
    /**
     * Awards food per pair of Inventor cards.
     */
    FOOD_PER_INVENTOR_PAIR_EFFECT,
    /**
     * Increases the hunting reward per Hunter owned.
     */
    HUNTING_REWARD_PER_HUNTER_EFFECT,
    /**
     * Prevents prestige loss when the player is the last Shaman.
     */
    NO_LOSS_LAST_SHAMAN_EFFECT,
    /**
     * Grants bonus stars in the Shaman track.
     */
    SHAMAN_BONUS_STARS_EFFECT,
    /**
     * Reduces the sustenance cost per character type.
     */
    SUSTENANCE_DISCOUNT_PER_TYPE_EFFECT
}
