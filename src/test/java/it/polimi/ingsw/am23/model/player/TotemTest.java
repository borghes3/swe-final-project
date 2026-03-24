package it.polimi.ingsw.am23.model.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TotemTest {

    @Test
    void constructorRejectsNullOwnerId() {
        assertThrows(NullPointerException.class, () -> new Totem(null, "red"));
    }

    @Test
    void gettersReturnValues() {
        Totem totem = new Totem("player-1", "blue");
        assertEquals("player-1", totem.getOwnerId());
        assertEquals("blue", totem.getColor());
    }
}
