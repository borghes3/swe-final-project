package it.polimi.ingsw.am23.model.resolvers;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.cards.EventCard;
import it.polimi.ingsw.am23.model.cards.events.SustenanceEventCard;

import java.util.ArrayList;
import java.util.List;

public class EventResolver {
    public void resolveEvents(List<EventCard> events, Game game){
        List<EventCard> orderedEvents = orderEvents(events);

        for(EventCard event: orderedEvents){
            event.resolve(game);
        }
    }

    private List<EventCard> orderEvents(List<EventCard> events){
        List<EventCard> normalEvents = new ArrayList<>();
        List<EventCard> sustenanceEvents = new ArrayList<>(); //nel caso in cui ce ne possano essere più di uno

        for(EventCard event: events){
            if(event instanceof SustenanceEventCard){
                sustenanceEvents.add(event);
            }else{
                normalEvents.add(event);
            }
        }
        normalEvents.addAll(sustenanceEvents);
        return normalEvents;
    }
}
