package it.polimi.ingsw.am23.jsonParsing.deserializers;

import com.google.gson.*;
import it.polimi.ingsw.am23.model.cards.EventCard;
import it.polimi.ingsw.am23.model.cards.events.CavePaintingsEventCard;
import it.polimi.ingsw.am23.model.cards.events.HuntingEventCard;
import it.polimi.ingsw.am23.model.cards.events.ShamanRitualEventCard;
import it.polimi.ingsw.am23.model.cards.events.SustenanceEventCard;
import it.polimi.ingsw.am23.model.enums.Era;

import java.lang.reflect.Type;

public class EventsDeserializer implements JsonDeserializer<EventCard> {

    public EventCard deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        JsonObject obj = jsonElement.getAsJsonObject();

        String id = obj.get("id").getAsString();
        Era era = Era.values()[obj.get("era").getAsInt() - 1];
        int points = obj.get("points").getAsInt();
        boolean isFinal = obj.get("isFinal").getAsBoolean();

        String eventType = obj.get("eventType").getAsString();

        JsonObject params = obj.has("eventParams")
                ? obj.getAsJsonObject("eventParams")
                : null;

        switch (eventType) {
            case "CAVE_PAINTINGS":
                assert params != null;
                int minArtists = params.get("minArtists").getAsInt();
                int pointsToRemove = params.get("pointsToRemove").getAsInt();
                int pointsRefactor = params.get("pointsFactor").getAsInt();
                return new CavePaintingsEventCard(id, era, points, isFinal, minArtists, pointsToRemove, pointsRefactor);
            case "HUNTING":
                assert params != null;
                int pointsPerHunter = params.get("pointsPerHunter").getAsInt();
                return new HuntingEventCard(id, era, points, isFinal, pointsPerHunter);
            case "SHAMAN_RITUAL":
                assert params != null;
                int winPoints = params.get("winPoints").getAsInt();
                int losePoints = params.get("losePoints").getAsInt();
                return new ShamanRitualEventCard(id, era, points, isFinal, winPoints, losePoints);
            case "SUSTENANCE":
                return new SustenanceEventCard(id, era, points, isFinal);
            default:
                throw new JsonParseException("Unknown event card type: " + eventType);
        }
    }
}
