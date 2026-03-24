package it.polimi.ingsw.am23.model.player;

import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.cards.characters.InventorCard;
import it.polimi.ingsw.am23.model.cards.characters.ShamanCard;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.InventionIcon;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;


public class Tribe {
    private final List<CharacterCard> characters;
    private final List<BuildingCard> buildings;

    public Tribe() {
        this.characters = new ArrayList<>();
        this.buildings = new ArrayList<>();
    }

    public void addCharacter(CharacterCard character) {
        this.characters.add(Objects.requireNonNull(character));
    }

    public void addBuilding(BuildingCard building) {
        this.buildings.add(Objects.requireNonNull(building));
    }

    public List<CharacterCard> getCharacters() {
        return List.copyOf(this.characters); //immutabile
    }

    public List<BuildingCard> getBuildings() {
        return List.copyOf(this.buildings);
    }

    public int count(CharacterType type) {
        int count = 0;
        for(CharacterCard card: characters) {
            if(card.getCharacterType() == type){
                count++;
            }
        }
        return count;
    }

    public int totalShamanStars(){
        int total = 0;
        for(CharacterCard card : characters) {
            if(card instanceof ShamanCard shaman){
                total += shaman.getStars();
            }
        }
        return total;
    }

    public int countCompletedSets(){
        int minCount = Integer.MAX_VALUE;

        for(CharacterType type : CharacterType.values()) {
            int currentCount = count(type);
            minCount = Math.min(minCount, currentCount);
        }
        return minCount == Integer.MAX_VALUE ? 0 : minCount;
    }

    public int countInventorPairsByIcon(){
        Map<InventionIcon, Integer> iconCounts = new EnumMap<>(InventionIcon.class);

        for(CharacterCard card : characters){
            if(card instanceof InventorCard inventor){
                InventionIcon icon = inventor.getIcon();
                iconCounts.put(icon, iconCounts.getOrDefault(icon, 0) + 1);
            }
        }
        int pairs = 0;
        for(int count : iconCounts.values()){
            pairs += count/2;
        }
        return pairs;
    }

    public boolean hasBuildings(){
        return !buildings.isEmpty();
    }

    // Restituisce il numero di icone degli inventori distinte
    public int getDistinctInventionIcons(){
        List<InventionIcon> icons = new ArrayList<>();
        for(CharacterCard card: characters){
            if(card instanceof InventorCard inventor){
                if(!icons.contains(inventor.getIcon())){
                    icons.add(inventor.getIcon());
                }
            }
        }
        return icons.size();
    }

}
