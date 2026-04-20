package it.polimi.ingsw.am23.setup.definition.cards;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public final class EventCardDefinition extends CardDefinition {
    private String eventType;
    private Map<String, Object> eventParams;
    @JsonProperty("isFinal")    // Override Jackson behaviour (see https://www.javadoc.io/doc/com.fasterxml.jackson.core/jackson-databind/latest/com/fasterxml/jackson/databind/MapperFeature.html#AUTO_DETECT_IS_GETTERS )
    private boolean isFinal;

    public EventCardDefinition() {
    }

    public String getEventType() {
        return eventType;
    }

    public Map<String, Object> getEventParams() {
        return eventParams;
    }

    public boolean isFinal() {
        return isFinal;
    }
}