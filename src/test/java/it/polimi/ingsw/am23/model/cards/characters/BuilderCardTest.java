package it.polimi.ingsw.am23.model.cards.characters;

import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.state.CharacterCardState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuilderCardTest {

    @Test
    void toStateContainsBuilderDiscount() {
        // Input  : BuilderCard("b", ERA_1, points=1, value=2, discount=2).
        // Output : toState() returns a CharacterCardState whose getDiscount()==2.
        BuilderCard card = new BuilderCard("b", Era.ERA_1, 1, 2, 2);
        CharacterCardState state = (CharacterCardState) card.toState();
        assertEquals(2, state.getDiscount());
    }
}
