package it.polimi.ingsw.am23.model.cards;

import it.polimi.ingsw.am23.model.Game;
import it.polimi.ingsw.am23.model.effects.BuildingEffect;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.player.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuildingCardTest {

    @Test
    void constructorRejectsNullEffect() {
        assertThrows(NullPointerException.class, () -> new BuildingCard("b1", Era.ERA_1, 0, 0, null));
    }

    @Test
    void onTakenAddsBuildingToTribe() {
        Player player = new Player("p1", "nick", 0, 0, "red");
        BuildingCard card = new BuildingCard("b1", Era.ERA_1, 0, 0, new BuildingEffect() {});

        card.onTaken(new Game(), player);

        assertTrue(player.getTribe().getBuildings().contains(card));
    }
}
