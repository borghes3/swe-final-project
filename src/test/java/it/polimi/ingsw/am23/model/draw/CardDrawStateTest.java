package it.polimi.ingsw.am23.model.draw;

import it.polimi.ingsw.am23.model.TestUtils;
import it.polimi.ingsw.am23.model.board.CardMarket;
import it.polimi.ingsw.am23.model.board.OfferAction;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.RowType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardDrawStateTest {

    @Test
    void initLimitsDrawsByAvailableCardsAndTracksCompletion() {
        // Input  : OfferTile with action(2,2,0); Market has 1 card on TOP and 1 on BOTTOM.
        //          init(state) → max draws per row capped at min(action, availableCards)=1; then take 1+1, then reset.
        // Output : after init isDrawingStarted()==true, canDraw on both rows true; after 1 draw each
        //          canDraw becomes false on both and hasFinishedDrawing()==true; after reset
        //          isDrawingStarted()==false and hasFinishedDrawing()==true (counts back to 0==0).
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