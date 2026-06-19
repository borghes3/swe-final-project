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
        // Input  : p1 with 3 shaman stars, p2 with 1; resolve ShamanRitualEventCard(winReward=5, loserPenalty=2).
        //          p1 has the most stars → wins +5 PP; p2 has the fewest → loses -2 PP.
        // Output : p1.getPrestigePoints()==5 and p2.getPrestigePoints()==-2.
        Player p1 = TestUtils.player("p1", 0, 0);
        Player p2 = TestUtils.player("p2", 0, 0);

        Game game = TestUtils.game(
                List.of(p1, p2),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, 0, null), new TurnOrderSlot(0, 0, null)),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        new it.polimi.ingsw.am23.model.cards.characters.ShamanCard("s1", Era.ERA_1, 0, 3, 2).onTaken(game, p1);
        new it.polimi.ingsw.am23.model.cards.characters.ShamanCard("s2", Era.ERA_1, 0, 1, 2).onTaken(game, p2);

        new ShamanRitualEventCard("sr", Era.ERA_1, 0, false, 5, 2).resolve(game);

        assertEquals(5, p1.getPrestigePoints());
        assertEquals(-2, p2.getPrestigePoints());
    }

    @Test
    void resolveSetsWinnerAndLoserToFalseWhenNoExtremes() {
        // Input  : 3 players each with PP=5 and 2 shaman stars; resolve ShamanRitualEventCard(winReward=5, loserPenalty=2).
        //          All tied → no clear winner/loser, the value/star multiplier still grants +3 PP each.
        // Output : every player ends with prestigePoints == 8 (5 + 3).
        Player p1 = TestUtils.player("p1", 0, 5);
        Player p2 = TestUtils.player("p2", 0, 5);
        Player p3 = TestUtils.player("p3", 0, 5);

        Game game = TestUtils.game(
                List.of(p1, p2, p3),
                List.of(new OfferTile('A', null, 3, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, 0, null), new TurnOrderSlot(0, 0, null), new TurnOrderSlot(0, 0, null)),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        new it.polimi.ingsw.am23.model.cards.characters.ShamanCard("s1", Era.ERA_1, 0, 2, 3).onTaken(game, p1);
        new it.polimi.ingsw.am23.model.cards.characters.ShamanCard("s2", Era.ERA_1, 0, 2, 3).onTaken(game, p2);
        new it.polimi.ingsw.am23.model.cards.characters.ShamanCard("s3", Era.ERA_1, 0, 2, 3).onTaken(game, p3);

        new ShamanRitualEventCard("sr", Era.ERA_1, 0, false, 5, 2).resolve(game);

        assertEquals(8, p1.getPrestigePoints());
        assertEquals(8, p2.getPrestigePoints());
        assertEquals(8, p3.getPrestigePoints());
    }
}
