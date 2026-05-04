package it.polimi.ingsw.am23.model.draw;

import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.board.CardMarket;
import it.polimi.ingsw.am23.model.board.OfferAction;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.RowType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CardDrawStateTest {

    @Test
    void initLimitsDrawsByAvailableCardsAndTracksCompletion() {
        CardDrawState state = new CardDrawState();
        OfferTile tile = new OfferTile('A', null, 2, new OfferAction(2, 2, 0));
        CardMarket market = new CardMarket(
                List.of(TestUtils.artist("t1", Era.ERA_1)),
                List.of(TestUtils.artist("b1", Era.ERA_1)),
                List.of()
        );

        state.init(tile, market);

        SelectedSingleCard topCard = new SelectedSingleCard(RowType.TOP, 0, false);
        SelectedSingleCard bottomCard = new SelectedSingleCard(RowType.BOTTOM, 0, false);

        assertTrue(state.isDrawingStarted());
        assertTrue(state.canDraw(topCard));
        assertTrue(state.canDraw(bottomCard));

        state.incrementDrawCount(topCard);
        state.incrementDrawCount(bottomCard);

        assertFalse(state.canDraw(topCard));
        assertFalse(state.canDraw(bottomCard));
        assertTrue(state.hasFinishedDrawing());

        state.reset();

        assertFalse(state.isDrawingStarted());
        assertTrue(state.hasFinishedDrawing());
    }
}