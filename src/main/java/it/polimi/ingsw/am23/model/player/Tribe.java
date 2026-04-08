package it.polimi.ingsw.am23.model.player;

import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.cards.characters.BuilderCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.InventionIcon;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

public class Tribe {

    private final List<CharacterCard> characters; //questa la teniamo per poi la view
    private final List<BuildingCard> buildings;

    private final EnumMap<CharacterType, Integer> characterCounts;
    private final EnumMap<InventionIcon, Integer> inventorIconCounts;
    private int totalShamanStars;

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

    public void addCharacter(CharacterCard character) {
        Objects.requireNonNull(character, "character cannot be null");

        this.characters.add(character);

        CharacterType type = character.getCharacterType();
        this.characterCounts.put(type, this.characterCounts.get(type) + 1);
    }

    public void addBuilding(BuildingCard building) {
        this.buildings.add(Objects.requireNonNull(building, "building cannot be null"));
    }

    public List<CharacterCard> getCharacters() {
        return List.copyOf(this.characters);
    }

    public List<BuildingCard> getBuildings() {
        return List.copyOf(this.buildings);
    }

    public int count(CharacterType type) {
        Objects.requireNonNull(type, "type cannot be null");
        return this.characterCounts.getOrDefault(type, 0);
    }

    public int totalShamanStars() {
        return this.totalShamanStars;
    }

    public int countCompletedSets() {
        int minCount = Integer.MAX_VALUE;

        for (CharacterType type : CharacterType.values()) {
            int currentCount = count(type);
            minCount = Math.min(minCount, currentCount);
        }

        return minCount == Integer.MAX_VALUE ? 0 : minCount;
    }

    public int countInventorPairsByIcon() {
        int pairs = 0;

        for (int count : inventorIconCounts.values()) {
            pairs += count / 2;
        }

        return pairs;
    }

    public boolean hasBuildings() {
        return !buildings.isEmpty();
    }

    public int getDistinctInventionIcons() {
        return inventorIconCounts.size();
    }

    public int getBuildingDiscount() {
        return characters.stream().filter(c -> c instanceof BuilderCard).mapToInt(b -> ((BuilderCard) b).getDiscount()).sum();
    }

    // ---- metodi di supporto usati dalle carte in onAddedToTribe() ----

    public void incrementInventorIconCount(InventionIcon icon) {
        Objects.requireNonNull(icon, "icon cannot be null");
        inventorIconCounts.put(icon, inventorIconCounts.getOrDefault(icon, 0) + 1);
    }

    public void addShamanStars(int stars) {
        if (stars < 0) {
            throw new IllegalArgumentException("stars cannot be negative");
        }
        totalShamanStars += stars;
    }
}