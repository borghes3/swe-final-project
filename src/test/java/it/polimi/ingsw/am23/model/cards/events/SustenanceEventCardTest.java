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

class SustenanceEventCardTest {

    @Test
    void resolveConsumesFoodOrPrestigeWithFallback() {
        // Input  : p1(food=5, PP=5) with 1 hunter; p2(food=0, PP=5) with 1 hunter; era ERA_2.
        //          Resolve SustenanceEventCard — each player must pay the sustenance food cost.
        // Output : p1 pays in food → p1.getFood()==4; p2 cannot pay in food → falls back to PP →
        //          p2.getPrestigePoints()==3 (lost 2 PP since 1 missing food => 2 PP).
        Player p1 = TestUtils.player("p1", 5, 5);
        Player p2 = TestUtils.player("p2", 0, 5);
        p1.getTribe().addCharacter(new it.polimi.ingsw.am23.model.cards.characters.HunterCard("h", Era.ERA_1, 0, false, 2));
        p2.getTribe().addCharacter(new it.polimi.ingsw.am23.model.cards.characters.HunterCard("h2", Era.ERA_1, 0, false, 2));

        Game game = TestUtils.game(
                List.of(p1, p2),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, 0, null), new TurnOrderSlot(0, 0, null)),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_2,
                1
        );

        SustenanceEventCard event = new SustenanceEventCard("s", Era.ERA_2, 0, false);
        event.resolve(game);

        assertEquals(4, p1.getFood());
        assertEquals(3, p2.getPrestigePoints());
    }
}
