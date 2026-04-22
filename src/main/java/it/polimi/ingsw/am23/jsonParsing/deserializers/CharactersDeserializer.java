package it.polimi.ingsw.am23.jsonParsing.deserializers;

import com.google.gson.*;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.cards.characters.*;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.InventionIcon;

import java.lang.reflect.Type;

public class CharactersDeserializer implements JsonDeserializer<CharacterCard> {

    public CharacterCard deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        JsonObject obj = jsonElement.getAsJsonObject();

        String id = obj.get("id").getAsString();
        Era era = Era.values()[obj.get("era").getAsInt() - 1];
        int points = obj.get("points").getAsInt();
        int minPlayers = obj.get("minPlayers").getAsInt();

        String characterType = obj.get("characterType").getAsString();

        Integer builderDiscount = obj.has("discount") ? obj.get("discount").getAsInt() : null;
        Boolean hasFoodSymbol = obj.has("hasFoodSymbol") ? obj.get("hasFoodSymbol").getAsBoolean() : null;
        String inventorIconString = obj.has("icon") ? obj.get("icon").getAsString() : null;
        Integer shamanStars = obj.has("stars") ? obj.get("stars").getAsInt() : null;

        switch (characterType) {
            case "ARTIST":
                return new ArtistCard(id, era, points, minPlayers);
            case "BUILDER":
                assert builderDiscount != null;
                return new BuilderCard(id, era, points, builderDiscount, minPlayers);
            case "GATHERER":
                return new GathererCard(id, era, points, minPlayers);
            case "HUNTER":
                assert hasFoodSymbol != null;
                return new HunterCard(id, era, points, hasFoodSymbol, minPlayers);
            case "INVENTOR":
                assert inventorIconString != null;
                InventionIcon icon = InventionIcon.valueOf(inventorIconString);
                return new InventorCard(id, era, points, icon, minPlayers);
            case "SHAMAN":
                assert shamanStars != null;
                return new ShamanCard(id, era, points, shamanStars, minPlayers);
            default:
                throw new JsonParseException("Unknown character type: " + characterType);
        }
    }
}
