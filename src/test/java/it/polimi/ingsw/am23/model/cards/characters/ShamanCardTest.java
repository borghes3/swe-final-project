package it.polimi.ingsw.am23.model.cards.characters;

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

class ShamanCardTest {

    @Test
    void onTakenAddsShamanStarsToTribe() {
        // Input  : ShamanCard("s", ERA_1, points=0, stars=3, value=2).onTaken(game, p).
        // Output : tribe.totalShamanStars()==3.
        Player p = TestUtils.player("p", 0, 0);
        Game game = TestUtils.game(
                List.of(p),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, 0,null)),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        new ShamanCard("s", Era.ERA_1, 0, 3, 2).onTaken(game, p);
        assertEquals(3, p.getTribe().totalShamanStars());
    }
}
