package it.polimi.ingsw.am23.model.cards;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.board.OfferAction;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.effects.buildings.FlatEndGamePointsEffect;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.state.BuildingCardState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuildingCardTest {

    @Test
    void canBeTakenAndOnTakenAddBuildingToTribe() {
        Player p = TestUtils.player("p1", 3, 0);
        Game game = TestUtils.game(
                List.of(p),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, null)),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        BuildingCard c = new BuildingCard("b1", Era.ERA_1, 2, 1, new FlatEndGamePointsEffect(3));
        assertTrue(c.canBeTaken());
        c.onTaken(game, p);

        assertEquals(1, p.getTribe().getBuildings().size());
        BuildingCardState state = (BuildingCardState) c.toState();
        assertEquals("FlatEndGamePointsEffect", state.getEffectId());
    }
}
