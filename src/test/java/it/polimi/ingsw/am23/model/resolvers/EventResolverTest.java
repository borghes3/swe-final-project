package it.polimi.ingsw.am23.model.resolvers;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.board.OfferAction;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.cards.EventCard;
import it.polimi.ingsw.am23.model.cards.events.SustenanceEventCard;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventResolverTest {

    @Test
    void resolveEventsExecutesSustenanceAfterAllOtherEventsAndOrdersByEra() {
        // Input  : events = [n3(ERA_3), s(SustenanceERA_1), n1(ERA_1), n2(ERA_2)]; resolve them.
        // Output : execution order = [N1, N2, N3, S] — non-sustenance events ordered by era,
        //          sustenance always last regardless of its era.
        List<String> executionOrder = new ArrayList<>();
        EventResolver resolver = new EventResolver();

        EventCard normalEra3 = new TrackingEvent("n3", Era.ERA_3, executionOrder, "N3");
        EventCard sustenance = new TrackingSustenance("s", Era.ERA_1, executionOrder, "S");
        EventCard normalEra1 = new TrackingEvent("n1", Era.ERA_1, executionOrder, "N1");
        EventCard normalEra2 = new TrackingEvent("n2", Era.ERA_2, executionOrder, "N2");

        Game game = minimalGame(List.of(TestUtils.player("p1", 3, 0)));
        resolver.resolveEvents(List.of(normalEra3, sustenance, normalEra1, normalEra2), game);

        assertEquals(List.of("N1", "N2", "N3", "S"), executionOrder);
    }

    private static Game minimalGame(List<Player> players) {
        return TestUtils.game(
                players,
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, 0,null)),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );
    }

    private static class TrackingEvent extends EventCard {
        private final List<String> log;
        private final String marker;

        private TrackingEvent(String id, Era era, List<String> log, String marker) {
            super(id, era, 0, false);
            this.log = log;
            this.marker = marker;
        }

        @Override
        public void resolve(Game game) {
            log.add(marker);
        }
    }

    private static class TrackingSustenance extends SustenanceEventCard {
        private final List<String> log;
        private final String marker;

        private TrackingSustenance(String id, Era era, List<String> log, String marker) {
            super(id, era, 0, false);
            this.log = log;
            this.marker = marker;
        }

        @Override
        public void resolve(Game game) {
            log.add(marker);
        }
    }
}
