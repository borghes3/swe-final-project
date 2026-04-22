package it.polimi.ingsw.am23.model.effects.buildings;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.board.OfferAction;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtraDrawEffectTest {

    @Test
    void onAfterAllActionsSetsPendingPlayerForExtraDraw() {
        Player p = TestUtils.player("p", 0, 0);
        Game game = TestUtils.game(
                List.of(p),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, 0,null)),
                List.of(TestUtils.artist("a1", Era.ERA_1)),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        ExtraDrawEffect effect = new ExtraDrawEffect();
        effect.onAfterAllActions(game, p);

        assertTrue(game.takeExtraCard("p", new it.polimi.ingsw.am23.model.cards.SelectedCardExtraDraw(0, null)).isSuccess());
    }
}
