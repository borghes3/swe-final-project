package it.polimi.ingsw.am23.model.cards.characters;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.CharacterType;


class ArtistCardTest {

    @Test
    void characterTypeIsArtist() {
        ArtistCard card = new ArtistCard("a1", Era.ERA_1, 0, 2);
        assertEquals(CharacterType.ARTIST, card.getCharacterType());
    }
}
