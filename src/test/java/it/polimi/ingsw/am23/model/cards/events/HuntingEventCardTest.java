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

class HuntingEventCardTest {

    @Test
    void resolveRewardsFoodAndPrestigeBasedOnHunters() {
        Player p1 = TestUtils.player("p1", 0, 0);
        p1.getTribe().addCharacter(new it.polimi.ingsw.am23.model.cards.characters.HunterCard("h1", Era.ERA_1, 0, false, 2));
        p1.getTribe().addCharacter(new it.polimi.ingsw.am23.model.cards.characters.HunterCard("h2", Era.ERA_1, 0, false, 2));

        Game game = TestUtils.game(
                List.of(p1),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, 0,null)),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        new HuntingEventCard("h", Era.ERA_1, 0, false, 2).resolve(game);

        assertEquals(2, p1.getFood());
        assertEquals(4, p1.getPrestigePoints());
    }
}
