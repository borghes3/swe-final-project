package it.polimi.ingsw.am23.jsonParsing.deserializers;

import com.google.gson.*;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderTile;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TurnOrderDeserializer implements JsonDeserializer<TurnOrderTile> {

    public TurnOrderTile deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject obj = jsonElement.getAsJsonObject();
        JsonArray jsonArray = obj.get("slots").getAsJsonArray();

        List<TurnOrderSlot> slots = new ArrayList<>();

        jsonArray.forEach(jsonSlotElement -> {
            JsonObject slotObject = jsonSlotElement.getAsJsonObject();

            slots.add(new TurnOrderSlot(
                    slotObject.get("position").getAsInt(),
                    slotObject.get("foodDelta").getAsInt(),
                    null
            ));
        });
        return new TurnOrderTile(slots);
    }
}
