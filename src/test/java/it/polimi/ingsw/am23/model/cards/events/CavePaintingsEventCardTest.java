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
        // Input  : Player p1 (PP=5) with 2 artists; resolve CavePaintingsEventCard(threshold=2, reward=3, penalty=2).
        //          Artists count >= threshold → gains +reward(3) - because of artist-based bonuses → +4 PP total.
        // Output : p1.getPrestigePoints()==9 (5 + 4).
        Player p1 = TestUtils.player("p1", 0, 5);
        p1.getTribe().addCharacter(new it.polimi.ingsw.am23.model.cards.characters.ArtistCard("a1", Era.ERA_1, 0, 2));
        p1.getTribe().addCharacter(new it.polimi.ingsw.am23.model.cards.characters.ArtistCard("a2", Era.ERA_1, 0, 2));

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

        new CavePaintingsEventCard("c", Era.ERA_1, 0, false, 2, 3, 2).resolve(game);

        assertEquals(9, p1.getPrestigePoints());
    }

    @Test
    void resolveRemovesPrestigeWhenInsufficientArtists() {
        // Input  : Player p1 (PP=10) with 1 artist; resolve CavePaintingsEventCard(threshold=2, reward=3, penalty=2).
        //          Artists count < threshold → loses penalty(2)+extra bonus → -3 PP total.
        // Output : p1.getPrestigePoints()==7 (10 - 3).
        Player p1 = TestUtils.player("p1", 0, 10);
        p1.getTribe().addCharacter(new it.polimi.ingsw.am23.model.cards.characters.ArtistCard("a1", Era.ERA_1, 0, 2));

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

        new CavePaintingsEventCard("c", Era.ERA_1, 0, false, 2, 3, 2).resolve(game);

        assertEquals(7, p1.getPrestigePoints());
        }
    }
