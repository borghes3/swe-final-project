package it.polimi.ingsw.am23.setup.definition.cards;

import java.util.Map;

public final class EventCardDefinition extends CardDefinition {
    private String eventType;
    private Map<String, Object> eventParams;
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