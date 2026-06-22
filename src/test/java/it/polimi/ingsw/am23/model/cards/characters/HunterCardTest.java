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

class HunterCardTest {

    @Test
    void onTakenAddsFoodBasedOnHuntersWhenCardHasFoodSymbol() {
        // Input  : Player p (food=0); add two HunterCards with foodSymbol=true.
        //          1st hunter: huntersInTribe=1 → +1 food; 2nd hunter: huntersInTribe=2 → +2 food.
        // Output : p.getFood()==3 (0 + 1 + 2).
        Player p = TestUtils.player("p", 0, 0);
        Game game = TestUtils.game(
                List.of(p),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, 0, null)),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        new HunterCard("h1", Era.ERA_1, 0, true, 2).onTaken(game, p);
        new HunterCard("h2", Era.ERA_1, 0, true, 2).onTaken(game, p);

        assertEquals(3, p.getFood());
    }

    @Test
    void onTakenDoesNotAddFoodWhenCardHasNoFoodSymbol() {
        // Input  : Player p (food=5); add two HunterCards with foodSymbol=false.
        // Output : p.getFood()==5 (unchanged — no food symbol means no on-taken food gain).
        Player p = TestUtils.player("p", 5, 0);
        Game game = TestUtils.game(
                List.of(p),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, 0, null)),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        new HunterCard("h1", Era.ERA_1, 0, false, 2).onTaken(game, p);
        new HunterCard("h2", Era.ERA_1, 0, false, 2).onTaken(game, p);

        assertEquals(5, p.getFood());
    }
}
