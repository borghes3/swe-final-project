package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.board.OfferAction;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtraDrawEffectTest {

    @Test
    void onBuildingAddedSetsPendingPlayerForExtraDraw() {
        // Input  : a player "p" with no food, a game with an artist available at TOP[0],
        //          then ExtraDrawEffect.onBuildingAdded(game, p) is invoked.
        // Output : game.takeExtraCard("p", TOP[0]) succeeds because the extra-draw pending
        //          player has been registered as "p" (so the artist can be taken for free).
        Player p = TestUtils.player("p", 0, 0);
        Game game = TestUtils.game(
                List.of(p),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, 0, null)),
                List.of(TestUtils.artist("a1", Era.ERA_1)),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        ExtraDrawEffect effect = new ExtraDrawEffect();
        effect.onBuildingAdded(game, p);

        assertTrue(game.takeExtraCard("p", new SelectedCardExtraDraw(0, null)).isSuccess());
    }
}
