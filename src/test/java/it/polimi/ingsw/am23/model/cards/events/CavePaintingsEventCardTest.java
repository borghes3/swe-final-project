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

class CavePaintingsEventCardTest {

    @Test
    void resolveAddsOrRemovesPrestigeDependingOnMinArtistsRule() {
        Player p1 = TestUtils.player("p1", 0, 5);
        p1.getTribe().addCharacter(new it.polimi.ingsw.am23.model.cards.characters.ArtistCard("a1", Era.ERA_1, 0, 2));
        p1.getTribe().addCharacter(new it.polimi.ingsw.am23.model.cards.characters.ArtistCard("a2", Era.ERA_1, 0, 2));

        Game game = TestUtils.game(
                List.of(p1),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0, null)),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        new CavePaintingsEventCard("c", Era.ERA_1, 0, false, 2, 3, 2).resolve(game);

        assertEquals(9, p1.getPrestigePoints());
    }
}
