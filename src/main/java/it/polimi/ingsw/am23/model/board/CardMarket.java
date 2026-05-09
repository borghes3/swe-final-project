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

public class CardMarket {
    private final List<Card> topRow;
    private List<Card> bottomRow;
    private final List<BuildingCard> topBuildings;
    private List<BuildingCard> bottomBuildings;

    public CardMarket(List<Card> topRow, List<Card> bottomRow, List<BuildingCard> topBuildings) {
        this.topRow = new ArrayList<>(topRow);
        this.bottomRow = new ArrayList<>(bottomRow);
        this.topBuildings = new ArrayList<>(topBuildings);
        this.bottomBuildings = new ArrayList<>();   // Non serve val di inizializzazione, la riga è vuota al setup
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


    public Card getCard(RowType selectedRow, int index) {
        List<Card> row = getRow(selectedRow);
        checkIndex(row, index);
        return row.get(index);
    }

    public void removeCard(RowType selectedRow, int index) {
        List<Card> row = getRow(selectedRow);
        checkIndex(row, index);
        row.remove(index);
    }

    public BuildingCard getBuilding(RowType selectedRow, int index) {
        List<BuildingCard> row = getBuildingRow(selectedRow);
        checkIndex(row, index);
        return row.get(index);
    }

    public void removeBuilding(RowType selectedRow, int index) {
        List<BuildingCard> row = getBuildingRow(selectedRow);
        checkIndex(row, index);
        row.remove(index);
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

    // Ritorna il numero di carte pescabili (quindi non Event Cards) per la row fornita
    public int getDrawableCount(RowType row) {
        int tribeCs = getRow(row).stream().filter(c -> !(c instanceof EventCard)).toList().size();
        int buildCs = getBuildingRow(row).size();
        return tribeCs + buildCs;
    }


    // RESOLVING PHASE
    public List<EventCard> getBottomRowEvents() {
        List<EventCard> events = new ArrayList<>();
        for (Card card : bottomRow) {
            if (card instanceof EventCard eventCard) {
                events.add(eventCard);
            }
        }
        return events;
    }

    public List<EventCard> getTopRowEvents() {       // Solo last-round resolving
        List<EventCard> events = new ArrayList<>();
        for (Card card : topRow) {
            if (card instanceof EventCard eventCard) {
                events.add(eventCard);
            }
        }
        return events;
    }


    // CLEANUP PHASE
    public void clearBottomRow() {
        bottomRow.clear();
    }

    public void moveTopRowToBottom() {
        bottomRow = new ArrayList<>(topRow);
        topRow.clear();
    }

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

            if (draw.getEra().ordinal() > currentEra.ordinal()) {
                result.registerEraAdvance(draw.getEra()); //segnala che c'è da fare il cambio era al Game
            }
        }
        return result;
    }


    // ERA PROGRESSION
    public void handleEraProgression(BuildingDeck nextEraDeck, Era newEra) {
        // Solo all'inizio di Era 3 si scartano i bottomBuildings
        if (newEra == Era.ERA_3) {
            bottomBuildings.clear();
        }
        // Sposto edifici da sopra a sotto
        bottomBuildings = new ArrayList<>(topBuildings);
        // Ripulisco la fila sopra e ripopolo con i nuovi buildings
        topBuildings.clear();
        while (!nextEraDeck.isEmpty(newEra)) {
            topBuildings.add(nextEraDeck.draw(newEra));
        }
    }

    public List<Card> getTopRow() {
        return List.copyOf(topRow);
    }

    public List<Card> getBottomRow() {
        return List.copyOf(bottomRow);
    }

    public List<BuildingCard> getTopBuildings() {
        return List.copyOf(topBuildings);
    }

    public List<BuildingCard> getBottomBuildings() {
        return List.copyOf(bottomBuildings);
    }

}
