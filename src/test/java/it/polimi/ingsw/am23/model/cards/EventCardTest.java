package it.polimi.ingsw.am23.model.cards;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.enums.Era;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventCardTest {

    @Test
    void eventCardsCannotBeTakenAndThrowOnOnTaken() {
        EventCard event = new EventCard("e", Era.ERA_1, 0, true) {
            @Override
            public void resolve(Game game) {
            }
        };

        assertFalse(event.canBeTaken());
        assertTrue(event.isFinal());
        assertThrows(UnsupportedOperationException.class, () -> event.onTaken(null, null));
        assertNotNull(event.toState());
    }
}
