package it.polimi.ingsw.am23.model.board;

import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.cards.EventCard;
import it.polimi.ingsw.am23.model.deck.BuildingDeck;
import it.polimi.ingsw.am23.model.deck.TribeDeck;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.RowType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The card market exposed to players during the drawing phase.
 * Holds two parallel pairs of rows: the tribe cards (top/bottom) and the
 * building cards (top/bottom). Provides lookup, removal and the cleanup /
 * refill helpers that are invoked at the end of each round and at era
 * progression.
 */
public class CardMarket {
    private final List<Card> topRow;
    private final List<BuildingCard> topBuildings;
    private List<Card> bottomRow;
    private List<BuildingCard> bottomBuildings;

    /**
     * Builds a new card market.
     *
     * @param topRow       cards to populate the top row at setup
     * @param bottomRow    cards to populate the bottom row at setup
     * @param topBuildings buildings to populate the top building row at setup
     */
    public CardMarket(List<Card> topRow, List<Card> bottomRow, List<BuildingCard> topBuildings) {
        this.topRow = new ArrayList<>(topRow);
        this.bottomRow = new ArrayList<>(bottomRow);
        this.topBuildings = new ArrayList<>(topBuildings);
        this.bottomBuildings = new ArrayList<>();   // The bottom buildings row is empty at setup
    }

    private <T> void checkIndex(List<T> list, int index) {
        if (index < 0 || index >= list.size()) {
            throw new IllegalArgumentException("invalid index " + index);
        }
    }

    private List<Card> getRow(RowType row) {
        return row == RowType.TOP ? topRow : bottomRow;
    }

    private List<BuildingCard> getBuildingRow(RowType row) {
        return row == RowType.TOP ? topBuildings : bottomBuildings;
    }


    /**
     * Returns the card at {@code index} in the supplied row.
     *
     * @param selectedRow row to inspect
     * @param index       0-based index within the row
     * @return the matching card
     */
    public Card getCard(RowType selectedRow, int index) {
        List<Card> row = getRow(selectedRow);
        checkIndex(row, index);
        return row.get(index);
    }

    /**
     * Removes the card at {@code index} from the supplied row.
     *
     * @param selectedRow row to mutate
     * @param index       0-based index within the row
     */
    public void removeCard(RowType selectedRow, int index) {
        List<Card> row = getRow(selectedRow);
        checkIndex(row, index);
        row.remove(index);
    }

    /**
     * Returns the building at {@code index} in the supplied row.
     *
     * @param selectedRow row to inspect
     * @param index       0-based index within the row
     * @return the matching building
     */
    public BuildingCard getBuilding(RowType selectedRow, int index) {
        List<BuildingCard> row = getBuildingRow(selectedRow);
        checkIndex(row, index);
        return row.get(index);
    }

    /**
     * Removes the building at {@code index} from the supplied row.
     *
     * @param selectedRow row to mutate
     * @param index       0-based index within the row
     */
    public void removeBuilding(RowType selectedRow, int index) {
        List<BuildingCard> row = getBuildingRow(selectedRow);
        checkIndex(row, index);
        row.remove(index);
    }


    /**
     * Appends a card to the supplied tribe row.
     *
     * @param row  destination row
     * @param card card to append
     */
    public void addCardToRow(RowType row, Card card) {
        getRow(row).add(card);
    }

    /**
     * Appends a building to the supplied building row.
     *
     * @param row      destination row
     * @param building building to append
     */
    public void addBuilding(RowType row, BuildingCard building) {
        getBuildingRow(row).add(building);
    }


    /**
     * @param row row to inspect; @return the current size of the tribe row
     */
    public int getRowSize(RowType row) {
        return getRow(row).size();
    }

    /**
     * Returns the number of drawable cards in the supplied row, i.e. tribe
     * cards (excluding event cards) plus buildings.
     *
     * @param row row to inspect
     * @return the drawable count
     */
    public int getDrawableCount(RowType row) {
        int tribeCs = getRow(row).stream().filter(c -> !(c instanceof EventCard)).toList().size();
        int buildCs = getBuildingRow(row).size();
        return tribeCs + buildCs;
    }


    /**
     * @return every event card currently sitting in the bottom row
     */
    public List<EventCard> getBottomRowEvents() {
        List<EventCard> events = new ArrayList<>();
        for (Card card : bottomRow) {
            if (card instanceof EventCard eventCard) {
                events.add(eventCard);
            }
        }
        return events;
    }

    /**
     * @return every event card currently sitting in the top row, used during
     * the final round resolution
     */
    public List<EventCard> getTopRowEvents() {
        List<EventCard> events = new ArrayList<>();
        for (Card card : topRow) {
            if (card instanceof EventCard eventCard) {
                events.add(eventCard);
            }
        }
        return events;
    }


    /**
     * Empties the bottom row at end of round, returning the discarded ids.
     *
     * @return the ids of the discarded cards
     */
    public List<String> clearBottomRow() {
        List<String> discardedIds = bottomRow.stream().map(Card::getId).toList();
        bottomRow.clear();
        return discardedIds;
    }

    /**
     * Moves the top row to the bottom row at end of round.
     *
     * @return the ids of the cards that have been moved
     */
    public List<String> moveTopRowToBottom() {
        List<String> movedIds = topRow.stream().map(Card::getId).toList();
        bottomRow = new ArrayList<>(topRow);
        topRow.clear();
        return movedIds;
    }

    /**
     * Refills the top row from the supplied tribe deck up to
     * {@code numberOfPlayers + 4} entries, also tracking whether the refill
     * triggered an era advance.
     *
     * @param tribeDeck       the deck to draw from
     * @param numberOfPlayers number of players in the match (used to compute the target size)
     * @param currentEra      current era; cards belonging to a higher era trigger an era advance
     * @return the {@link RefillResult} describing the operation
     * @throws IllegalArgumentException if {@code numberOfPlayers} is less than 2
     */
    public RefillResult refillTopRow(TribeDeck tribeDeck, int numberOfPlayers, Era currentEra) {
        Objects.requireNonNull(tribeDeck, "tribeDeck cannot be null");
        Objects.requireNonNull(currentEra, "currentEra cannot be null");

        if (numberOfPlayers <= 1) {
            throw new IllegalArgumentException("Number of Players must be at least 2.");
        }

        int targetSize = numberOfPlayers + 4;
        RefillResult result = new RefillResult();

        while (topRow.size() < targetSize && !tribeDeck.isEmpty()) {
            Card draw = tribeDeck.draw();
            topRow.add(draw);
            result.registerAddedCard(draw);

            if (draw.getEra().ordinal() > currentEra.ordinal()) {
                result.registerEraAdvance(draw.getEra());
            }
        }
        return result;
    }


    /**
     * Handles an era progression by rotating the building rows and refilling
     * the top row from the supplied building deck for the new era.
     *
     * @param nextEraDeck the building deck containing the cards for {@code newEra}
     * @param newEra      the era the match is transitioning into
     * @return the {@link EraProgressionResult} describing the operation
     */
    public EraProgressionResult handleEraProgression(BuildingDeck nextEraDeck, Era newEra) {
        List<BuildingCard> discarded = new ArrayList<>();

        // Only at the beginning of Era 3 the bottom buildings are discarded
        if (newEra == Era.ERA_3) {
            discarded.addAll(bottomBuildings);
            bottomBuildings.clear();
        }
        // Move buildings from top to bottom
        bottomBuildings = new ArrayList<>(topBuildings);
        // Clear the top row and refill with the new era buildings
        topBuildings.clear();
        while (!nextEraDeck.isEmpty(newEra)) {
            topBuildings.add(nextEraDeck.draw(newEra));
        }
        return new EraProgressionResult(List.copyOf(topBuildings), List.copyOf(discarded));
    }

    /**
     * @return an unmodifiable copy of the top tribe row
     */
    public List<Card> getTopRow() {
        return List.copyOf(topRow);
    }

    /**
     * @return an unmodifiable copy of the bottom tribe row
     */
    public List<Card> getBottomRow() {
        return List.copyOf(bottomRow);
    }

    /**
     * @return an unmodifiable copy of the top building row
     */
    public List<BuildingCard> getTopBuildings() {
        return List.copyOf(topBuildings);
    }

    /**
     * @return an unmodifiable copy of the bottom building row
     */
    public List<BuildingCard> getBottomBuildings() {
        return List.copyOf(bottomBuildings);
    }

}
