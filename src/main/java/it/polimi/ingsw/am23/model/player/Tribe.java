package it.polimi.ingsw.am23.model.player;

import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.InventionIcon;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

/**
 * The collection of cards owned by a single player.
 * Holds the character and building cards plus the aggregate counters used
 * by scoring rules and building effects (per character-type count,
 * per-icon count of inventors and total shaman stars).
 */
public class Tribe {

    private final List<CharacterCard> characters;
    private final List<BuildingCard> buildings;

    private final EnumMap<CharacterType, Integer> characterCounts;
    private final EnumMap<InventionIcon, Integer> inventorIconCounts;
    private int totalShamanStars;

    /** Builds an empty tribe. */
    public Tribe() {
        this.characters = new ArrayList<>();
        this.buildings = new ArrayList<>();

        this.characterCounts = new EnumMap<>(CharacterType.class);
        for (CharacterType type : CharacterType.values()) {
            this.characterCounts.put(type, 0);
        }

        this.inventorIconCounts = new EnumMap<>(InventionIcon.class);
        this.totalShamanStars = 0;
    }

    /**
     * Adds a character card and updates the per-type counter.
     *
     * @param character the character to add (must be non null)
     */
    public void addCharacter(CharacterCard character) {
        Objects.requireNonNull(character, "character cannot be null");

        this.characters.add(character);

        CharacterType type = character.getCharacterType();
        this.characterCounts.put(type, this.characterCounts.get(type) + 1);
    }

    /**
     * Adds a building card.
     *
     * @param building the building to add (must be non null)
     */
    public void addBuilding(BuildingCard building) {
        this.buildings.add(Objects.requireNonNull(building, "building cannot be null"));
    }

    /** @return an unmodifiable copy of the character cards collected so far */
    public List<CharacterCard> getCharacters() {
        return List.copyOf(this.characters);
    }

    /** @return an unmodifiable copy of the building cards collected so far */
    public List<BuildingCard> getBuildings() {
        return List.copyOf(this.buildings);
    }

    /**
     * @param type character type to count (must be non null)
     * @return the number of characters of {@code type} owned by this tribe
     */
    public int count(CharacterType type) {
        Objects.requireNonNull(type, "type cannot be null");
        return this.characterCounts.getOrDefault(type, 0);
    }

    /** @return the total number of shaman stars accumulated by this tribe */
    public int totalShamanStars() {
        return this.totalShamanStars;
    }

    /**
     * Number of completed character sets, computed as the minimum count
     * across all character types.
     *
     * @return the number of completed sets
     */
    public int countCompletedSets() {
        int minCount = Integer.MAX_VALUE;

        for (CharacterType type : CharacterType.values()) {
            int currentCount = count(type);
            minCount = Math.min(minCount, currentCount);
        }

        return minCount == Integer.MAX_VALUE ? 0 : minCount;
    }

    /**
     * Number of inventor pairs that can be formed grouping by icon.
     *
     * @return the number of inventor pairs
     */
    public int countInventorPairsByIcon() {
        int pairs = 0;

        for (int count : inventorIconCounts.values()) {
            pairs += count / 2;
        }

        return pairs;
    }

    /** @return {@code true} if the tribe owns at least one building */
    public boolean hasBuildings() {
        return !buildings.isEmpty();
    }

    /** @return the number of distinct invention icons present in the tribe */
    public int getDistinctInventionIcons() {
        return inventorIconCounts.size();
    }

    /** @return the cumulative food discount granted by the owned characters when buying buildings */
    public int getBuildingDiscount() {
        return characters.stream()
                .mapToInt(CharacterCard::getDiscount)
                .sum();
    }

    /**
     * Increments the per-icon counter for the supplied invention icon.
     * Invoked by Inventor cards upon being added to the tribe.
     *
     * @param icon icon whose counter should be incremented (must be non null)
     */
    public void incrementInventorIconCount(InventionIcon icon) {
        Objects.requireNonNull(icon, "icon cannot be null");
        inventorIconCounts.put(icon, inventorIconCounts.getOrDefault(icon, 0) + 1);
    }

    /**
     * Adds shaman stars to the tribe's total.
     *
     * @param stars number of stars to add (must be non negative)
     * @throws IllegalArgumentException if {@code stars} is negative
     */
    public void addShamanStars(int stars) {
        if (stars < 0) {
            throw new IllegalArgumentException("stars cannot be negative");
        }
        totalShamanStars += stars;
    }
}
