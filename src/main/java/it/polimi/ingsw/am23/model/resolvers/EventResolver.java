package it.polimi.ingsw.am23.model.resolvers;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.EventCard;

import java.util.Comparator;
import java.util.List;

/**
 * Resolves event cards against a {@link Game} instance.
 * Provides a global resolution helper that sorts events by priority and
 * era and a single-event helper used during the end-of-round notification
 * cycle.
 */
public class EventResolver {

    /**
     * Resolves every event in the supplied list, applying priority and
     * era ordering before triggering each event.
     *
     * @param events events to resolve
     * @param game   game instance the events are applied to
     */
    public void resolveEvents(List<EventCard> events, Game game) {
        for (EventCard event : orderEvents(events)) {
            event.resolve(game);
        }
    }

    /**
     * Resolves a single event against the supplied game.
     *
     * @param event event to resolve
     * @param game  game instance to apply the event to
     */
    public void resolveSingleEvent(EventCard event, Game game) {
        event.resolve(game);
    }

    /**
     * Orders the supplied events by ascending resolution priority and,
     * within the same priority, by ascending era ordinal.
     *
     * @param events events to order
     * @return the ordered list of events
     */
    public List<EventCard> orderEvents(List<EventCard> events) {
        return events.stream()
                .sorted(
                        Comparator.comparingInt(EventCard::getResolutionPriority)
                                .thenComparingInt(e -> e.getEra().ordinal())
                )
                .toList();
    }
}
