package it.polimi.ingsw.am23.model.cards;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharacterCardTest {

    @Test
    void onTakenAddsCharacterToTribe() {
        Player player = new Player("p1", "nick", 0, 0, "red");
        CharacterCard card = new TestCharacterCard("c1", Era.ERA_1, 1, CharacterType.BUILDER);

        card.onTaken(new Game(), player);

        assertTrue(player.getTribe().getCharacters().contains(card));
    }

    private static class TestCharacterCard extends CharacterCard {
        TestCharacterCard(String id, Era era, int points, CharacterType type) {
            super(id, era, points, type);
        }
    }
}
