package it.polimi.ingsw.am23.jsonParsing.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.polimi.ingsw.am23.model.board.OfferAction;
import it.polimi.ingsw.am23.model.board.OfferTile;

import java.lang.reflect.Type;

public class OfferTilesDeserializer implements JsonDeserializer<OfferTile> {

    public OfferTile deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        JsonObject obj = jsonElement.getAsJsonObject();

        char id = obj.get("id").getAsString().charAt(0);
        int minPlayers = obj.get("minPlayers").getAsInt();

        int topDrawCount = obj.get("topDrawCount").getAsInt();
        int bottomDrawCount = obj.get("bottomDrawCount").getAsInt();
        int foodReward = obj.get("foodReward").getAsInt();

        OfferAction action = new OfferAction(topDrawCount, bottomDrawCount, foodReward);

        return new OfferTile(id, null, minPlayers, action);
    }
}
