package it.polimi.ingsw.am23.model.cards;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventCardTest {

    @Test
    void eventCardsCannotBeTaken() {
        EventCard card = new TestEventCard("e1", Era.ERA_1);
        assertFalse(card.canBeTaken());
    }

    @Test
    void onTakenThrowsUnsupportedOperation() {
        EventCard card = new TestEventCard("e1", Era.ERA_1);
        assertThrows(UnsupportedOperationException.class, () -> card.onTaken(new Game(), new Player("p1", "nick", 0, 0, "red")));
    }

    private static class TestEventCard extends EventCard {
        TestEventCard(String id, Era era) {
            super(id, era, 0);
        }

        @Override
        public void resolve(Game game) {
        }
    }
}
