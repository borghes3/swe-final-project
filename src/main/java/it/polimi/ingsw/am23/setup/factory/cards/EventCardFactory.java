package it.polimi.ingsw.am23.setup.factory.cards;

import it.polimi.ingsw.am23.model.cards.EventCard;
import it.polimi.ingsw.am23.model.cards.events.CavePaintingsEventCard;
import it.polimi.ingsw.am23.model.cards.events.HuntingEventCard;
import it.polimi.ingsw.am23.model.cards.events.ShamanRitualEventCard;
import it.polimi.ingsw.am23.model.cards.events.SustenanceEventCard;
import it.polimi.ingsw.am23.setup.definition.cards.EventCardDefinition;

import java.util.Map;
import java.util.Objects;

public class EventCardFactory {

    public EventCard create(EventCardDefinition definition) {
        Objects.requireNonNull(definition, "definition cannot be null");

        Map<String, Object> params = definition.getEventParams() != null
                ? definition.getEventParams()
                : Map.of();

        return switch (definition.getEventType()) {
            case "CAVE_PAINTINGS" -> new CavePaintingsEventCard(
                    definition.getId(),
                    definition.getEra(),
                    definition.getPoints(),
                    definition.isFinal(),
                    readInt(params, "minArtists"),
                    readInt(params, "pointsToRemove"),
                    readInt(params, "pointsFactor")
            );

            case "HUNTING" -> new HuntingEventCard(
                    definition.getId(),
                    definition.getEra(),
                    definition.getPoints(),
                    definition.isFinal(),
                    readInt(params, "pointsPerHunter")
            );

            case "SHAMAN_RITUAL" -> new ShamanRitualEventCard(
                    definition.getId(),
                    definition.getEra(),
                    definition.getPoints(),
                    definition.isFinal(),
                    readInt(params, "winPoints"),
                    readInt(params, "losePoints")
            );

            case "SUSTENANCE" -> new SustenanceEventCard(
                    definition.getId(),
                    definition.getEra(),
                    definition.getPoints(),
                    definition.isFinal()
            );

            default -> throw new IllegalArgumentException(
                    "Unsupported event type: " + definition.getEventType()
            );
        };
    }

    private int readInt(Map<String, Object> params, String key) {
        Object value = params.get(key);

        if (value == null) {
            throw new IllegalArgumentException("Missing event parameter: " + key);
        }

        if (value instanceof Integer integerValue) {
            return integerValue;
        }

        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }

        throw new IllegalArgumentException("Event parameter '" + key + "' is not a number");
    }
}
