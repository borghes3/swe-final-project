package it.polimi.ingsw.am23.model.cards.events;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.board.OfferAction;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShamanRitualEventCardTest {

    @Test
    void resolveAssignsWinAndLossByStarRanking() {
        Player p1 = TestUtils.player("p1", 0, 0);
        Player p2 = TestUtils.player("p2", 0, 0);

        Game game = TestUtils.game(
                List.of(p1, p2),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, 0,null), new TurnOrderSlot(0, 0,null)),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

            // In produzione le stelle vengono aggiornate nel percorso onTaken.
            new it.polimi.ingsw.am23.model.cards.characters.ShamanCard("s1", Era.ERA_1, 0, 3, 2).onTaken(game, p1);
            new it.polimi.ingsw.am23.model.cards.characters.ShamanCard("s2", Era.ERA_1, 0, 1, 2).onTaken(game, p2);

        new ShamanRitualEventCard("sr", Era.ERA_1, 0, false, 5, 2).resolve(game);

        assertEquals(5, p1.getPrestigePoints());
        assertEquals(-2, p2.getPrestigePoints());
    }
}
