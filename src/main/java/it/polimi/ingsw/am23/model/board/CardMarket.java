package it.polimi.ingsw.am23.model.board;

import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.deck.BuildingDeck;
import it.polimi.ingsw.am23.model.deck.TribeDeck;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.RowType;
import it.polimi.ingsw.am23.model.cards.EventCard;
import it.polimi.ingsw.am23.model.state.CardState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CardMarket {
    private List<Card> topRow;
    private List<Card> bottomRow;
    private final List<BuildingCard> topBuildings;
    private List<BuildingCard> bottomBuildings;

    public CardMarket(List<Card> topRow, List<Card> bottomRow, List<BuildingCard> topBuildings, List<BuildingCard> bottomBuildings) {
        this.topRow = new ArrayList<Card>(topRow);
        this.bottomRow = new ArrayList<>(bottomRow);
        this.topBuildings = new ArrayList<>(topBuildings);
        this.bottomBuildings = new ArrayList<>(bottomBuildings);
    }

    public List<Card> getTopRow() {
        return topRow;
    }

    public List<Card> getBottomRow() {
        return bottomRow;
    }

    public List<BuildingCard> getTopBuildings() {
        return topBuildings;
    }

    public List<BuildingCard> getBottomBuildings() {
        return bottomBuildings;
    }

    public Card getCard(RowType selectedRow, int index) {
        List<Card> row = getRow(selectedRow);
        checkIndex(row, index);
        return row.get(index);
    }

    public Card removeCard(RowType selectedRow, int index) {
        List<Card> row = getRow(selectedRow);
        checkIndex(row, index);
        return row.remove(index);
    }

    public BuildingCard getBuilding(RowType selectedRow, int index) {
        List<BuildingCard> row = getBuildingRow(selectedRow);
        checkIndex(row, index);
        return row.get(index);
    }

    public BuildingCard removeBuilding(RowType selectedRow, int index) {
        List<BuildingCard> row = getBuildingRow(selectedRow);
        checkIndex(row, index);
        return row.remove(index);
    }

    public void addCardToRow(RowType row, Card card) {
        getRow(row).add(card);
    }

    public void addBuilding(RowType row, BuildingCard building) {
        getBuildingRow(row).add(building);
    }

    public int getRowSize(RowType row) {
        return getRow(row).size();
    }

    public int getBuildingCount(RowType row) {
        return getBuildingRow(row).size();
    }

    public RefillResult refillTopRow(TribeDeck tribeDeck, int targetSize, Era currentEra) {
        Objects.requireNonNull(tribeDeck, "tribeDeck cannot be null");
        Objects.requireNonNull(currentEra, "currentEra cannot be null");

        if (targetSize < 0) {
            throw new IllegalArgumentException("targetsize cannot be negative");
        }
        RefillResult result = new RefillResult();

        while (topRow.size() < targetSize && !tribeDeck.isEmpty()) {
            Card draw = tribeDeck.draw();
            topRow.add(draw);

            if (draw.getEra().ordinal() > currentEra.ordinal()) {
                result.registerEraAdvance(draw.getEra()); //segnala che c'è da fare il cambio era al Game
            } // ordinal restituisce la posizione nell'enum
        }
        return result;
    }

    private List<Card> getRow(RowType row) {
        return row == RowType.TOP ? topRow : bottomRow;
    }

    private List<BuildingCard> getBuildingRow(RowType row) {
        return row == RowType.TOP ? topBuildings : bottomBuildings;
    }

    private <T> void checkIndex(List<T> list, int index) {
        if (index < 0 || index >= list.size()) {
            throw new IndexOutOfBoundsException("invalid index" + index);
        }
    }

    //gestione eventi
    public List<EventCard> getBottomRowEvents() {
        List<EventCard> events = new ArrayList<>();
        for (Card card : bottomRow) {
            if (card instanceof EventCard eventCard) {
                events.add(eventCard);
            }
        }
        return events;
    }

    public void clearBottomRow() { //da chiamare dopo aver risolto gli eventi
        bottomRow.clear();
    }

    public void moveTopRowToBottom() {
        bottomRow = new ArrayList<>(topRow);
        topRow.clear();
    }

    public void advanceBuildingEra(BuildingDeck nextEraDeck, Era newEra) {
        bottomBuildings.clear();
        bottomBuildings = new ArrayList<>(topBuildings);
        topBuildings.clear();

        while (!nextEraDeck.isEmpty(newEra)) {
            topBuildings.add(nextEraDeck.draw(newEra));
        }
    }


}
