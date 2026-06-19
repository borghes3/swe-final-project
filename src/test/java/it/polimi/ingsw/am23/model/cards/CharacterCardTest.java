package it.polimi.ingsw.am23.model.cards;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.board.OfferAction;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CharacterCardTest {

    @Test
    void onTakenValidatesArgumentsAndAddsCharacter() {
        // Input  : a fresh CharacterCard (type=ARTIST, value=2); first call onTaken(null, null),
        //          then call onTaken(game, p) with a valid player p.
        // Output : onTaken(null, null) throws NullPointerException; valid onTaken adds 1 character
        //          to the tribe and toState() is not null.
        CharacterCard card = new CharacterCard("c", Era.ERA_1, 0, CharacterType.ARTIST, 2) {
        };

        assertThrows(NullPointerException.class, () -> card.onTaken(null, null));

        Player p = TestUtils.player("p", 0, 0);
        Game game = TestUtils.game(
                List.of(p),
                List.of(new OfferTile('A', null, 2, new OfferAction(0, 0, 0))),
                List.of(new TurnOrderSlot(0,0, null)),
                List.of(),
                List.of(),
                List.of(),
                Era.ERA_1,
                1
        );

        card.onTaken(game, p);
        assertEquals(1, p.getTribe().getCharacters().size());
        assertNotNull(card.toState());
    }
}
