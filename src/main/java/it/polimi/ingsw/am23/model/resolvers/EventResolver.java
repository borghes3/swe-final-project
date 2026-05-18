package it.polimi.ingsw.am23.model.resolvers;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.EventCard;

import java.util.Comparator;
import java.util.List;

public class EventResolver {

    public void resolveEvents(List<EventCard> events, Game game) {
        for (EventCard event : orderEvents(events)) {
            event.resolve(game);
        }
    }

    public void resolveSingleEvent(EventCard event, Game game) {
        event.resolve(game);
    }

    public List<EventCard> orderEvents(List<EventCard> events) {
        return events.stream()
                .sorted(
                        Comparator.comparingInt(EventCard::getResolutionPriority)
                                .thenComparingInt(e -> e.getEra().ordinal())
                )
                .toList();
    }
}