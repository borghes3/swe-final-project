package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.exceptions.*;               // tutte le exceptions custom
import it.polimi.ingsw.am23.model.board.*;
import it.polimi.ingsw.am23.model.cards.*;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderTile;
import it.polimi.ingsw.am23.model.deck.BuildingDeck;
import it.polimi.ingsw.am23.model.deck.TribeDeck;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.enums.RowType;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.resolvers.EventResolver;
import it.polimi.ingsw.am23.model.resolvers.ScoreCalculator;
import it.polimi.ingsw.am23.model.resolvers.ScoreResult;
import it.polimi.ingsw.am23.model.state.GameState;

import java.util.*;

public class Game implements GameModel {

    // Attributi
    private final List<Player> players;
    private final Board board;
    private final TribeDeck tribeDeck;
    private final BuildingDeck buildingDeck;
    private final EventResolver eventResolver;
    private final CardMarket cardMarket;
    private final List<ModelObserver> observers = new ArrayList<>();

    // Stato partita
    private GameState gameState;
    private Era currentEra;
    private int currentRound;
    private GamePhase phase;
    private String pendingExtraDrawPlayerId;

    public Game(List<Player> players, Board board, TribeDeck tribeDeck, BuildingDeck buildingDeck, EventResolver eventResolver, CardMarket cardMarket, Era currentEra, int currentRound) {
        this.players = players;
        this.board = board;
        this.tribeDeck = tribeDeck;
        this.buildingDeck = buildingDeck;
        this.eventResolver = eventResolver;
        this.cardMarket = cardMarket;
        this.currentEra = currentEra;
        this.currentRound = currentRound;
        this.phase = GamePhase.SETUP;
    }


    // Setup completato, comunico al controller setup terminato
    public void startGame() {
        phase = GamePhase.PLACING_TOTEMS;
        notifyGameStarted();
    }

    public Player findPlayer(String playerId) {
        return players.stream()
                .filter(p -> Objects.equals(p.getId(), playerId))
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException("Player " + playerId + " not found in this match"));
    }

    // ------------------------------------------
    // PLACING PHASE
    // ------------------------------------------
    @Override
    public ActionResult placeTotem(String playerId, char offerTileChar) {
        Player p = findPlayer(playerId);
        // Verifico che sia lui il prossimo a dover piazzare il totem
        if (!Objects.equals(board.getTurnOrderTile().getFirstFreeSlot().getPlayerId(), p.getId())) {
            // TODO: ActionResult + Errore
            throw new IllegalActionException("It's not your turn.");
        }

        // Verifico che la tile sia vuota
        OfferTile tile = board.getOfferTile(offerTileChar);
        if (!tile.isFree()) {
            // TODO: ActionResult + Errore
            throw new TileNotEmptyException("The selected offer tile is not empty");
        }
        // Rimuovo il totem dalla tessera ordine di turno
        board.findTurnOrderSlotOccupiedBy(playerId).clear();
        // E lo piazzo nella offer tile selezionata
        tile.placeTotem(p.getId());

        // Se tutti hanno piazzato, la tessera ordine di turno é vuota, notifico il controller
        if (board.getTurnOrderTile().isEmpty()) {
            this.phase = GamePhase.RESOLVING_OFFERS;
            gameState = buildGameState();
            notifyEndOfPlacingPhase();
        }

        gameState = buildGameState();
        notifyGameStateChanged();
        return new ActionResult(gameState);
    }

    // ------------------------------------------
    // DRAWING PHASE
    // ------------------------------------------
    @Override
    public ActionResult takeCards(String playerId, SelectedCards selectedCards) {
        Player p = findPlayer(playerId);
        OfferTile tile = board.getFirstOccupiedOfferTile();

        // Verifico sia il prossimo player a poter pescare
        if (!Objects.equals(p.getId(), tile.getOccupiedByPlayerId())) {
            // TODO: ActionResult + Errore
            throw new IllegalActionException("It's not your turn.");
        }

        // Verifico che il numero di carte richieste sia corretto
        if (!checkDrawingCriteria(tile, selectedCards)) {
            throw new IllegalActionException("Invalid number of cards.");
        }

        // Food reward se presente sul Offer Tile
        p.addFood(tile.getAction().getFoodReward());

        // Aggiungo le carte alla tribù del Player

        int foodDiscount = p.getTribe().getBuildingDiscount();

        // LOWER ROW
        // Tribe Cards
        for (int boardIndex : selectedCards.getLowerRow()) {
            Card c = cardMarket.getCard(RowType.BOTTOM, boardIndex);
            // Verifico sia prendibile
            if (!c.canBeTaken()) {
                // TODO: ActionResult + Errore
                throw new CardNotDrawableException("This card cannot be drawn from the card market");
            }
            // Aggiungo alla tribe e rimuovo dal market
            cardMarket.removeCard(RowType.BOTTOM, boardIndex);
            c.onTaken(this, p);
        }
        // Buildings
        for (int boardIndex : selectedCards.getLowerBuildings()) {
            BuildingCard c = cardMarket.getBuilding(RowType.BOTTOM, boardIndex);
            // Verifico costo
            if (c.getFoodCost() - foodDiscount > p.getFood()) {
                // TODO: ActionResult + Errore
                throw new CardNotDrawableException("The food cost exceeds the player's reserve");
            }
            // Aggiungo alla tribe e rimuovo cibo dal player
            cardMarket.removeBuilding(RowType.BOTTOM, boardIndex);
            c.onTaken(this, p);
            p.spendFood(c.getFoodCost() - foodDiscount);
        }

        // UPPER ROW
        // Tribe Cards
        for (int boardIndex : selectedCards.getUpperRow()) {
            Card c = cardMarket.getCard(RowType.TOP, boardIndex);
            // Verifico sia prendibile
            if (!c.canBeTaken()) {
                // TODO: ActionResult + Errore
                throw new CardNotDrawableException("This card cannot be drawn from the card market");
            }
            // Aggiungo alla tribe e rimuovo dal market
            cardMarket.removeCard(RowType.TOP, boardIndex);
            c.onTaken(this, p);
        }
        // Buildings
        for (int boardIndex : selectedCards.getUpperBuildings()) {
            BuildingCard c = cardMarket.getBuilding(RowType.TOP, boardIndex);
            // Verifico costo
            if (c.getFoodCost() - foodDiscount > p.getFood()) {
                // TODO: ActionResult + Errore
                throw new CardNotDrawableException("The food cost exceeds the player's reserve");
            }
            // Aggiungo alla tribe e rimuovo cibo dal player
            cardMarket.removeBuilding(RowType.TOP, boardIndex);
            c.onTaken(this, p);
            p.spendFood(c.getFoodCost() - foodDiscount);
        }

        // Ritorno al turn order
        returnToTurnOrder(playerId);

        // Se tutti hanno pescato, il tracciato è vuoto, notifico il controller
        if (board.getFirstOccupiedOfferTile() == null) {
            phase = GamePhase.RESOLVING_EVENTS;
            gameState = buildGameState();
            notifyEndOfDrawingPhase();
            // Se è stato settato un player che deve fare extra draw, lo notifico ora
            if (pendingExtraDrawPlayerId != null) {
                phase = GamePhase.EXTRA_DRAW;
                gameState = buildGameState();
                notifyExtraDrawRequest();
            }
        }

        gameState = buildGameState();
        notifyGameStateChanged();
        return new ActionResult(gameState);
    }

    // Verifica condizioni del payload delle carte da pescare selezionate
    private boolean checkDrawingCriteria(OfferTile tile, SelectedCards selectedCards) {
        // UPPER ROW
        if ((selectedCards.getUpperRow().size() + selectedCards.getUpperBuildings().size()) != Math.min(tile.getAction().getUpperDrawRowCount(), cardMarket.getDrawableCount(RowType.TOP)))
            return false;
        // BOTTOM ROW
        if ((selectedCards.getLowerRow().size() + selectedCards.getLowerBuildings().size()) != Math.min(tile.getAction().getBottomDrawCount(), cardMarket.getDrawableCount(RowType.BOTTOM)))
            return false;
        return true;
    }

    // Dopo aver finito di pescare, ritorno al turn order tile
    private void returnToTurnOrder(String playerId) {
        // Rimuovo il player dal Offer Tile in cui si trova
        OfferTile offerTile = board.getOfferTileByPlayerId(playerId);
        offerTile.clear();
        // Recupero primo slot libero e posiziono il player
        TurnOrderTile turnTile = board.getTurnOrderTile();
        TurnOrderSlot slot = turnTile.getFirstFreeSlot();
        slot.placeTotem(playerId);

        // Gestione delta cibo del turn order slot
        if (slot.givesFood()) {                                     // Delta positivo
            findPlayer(playerId).addFood(slot.getFoodDelta());
        } else if (slot.getFoodDelta() != 0) {                      // Delta negativo
            if (findPlayer(playerId).canAfford(slot.getFoodDelta()))
                findPlayer(playerId).spendFood(slot.getFoodDelta());
            else
                findPlayer(playerId).spendPrestigePoints(slot.getFoodDelta() * 2);
        }
    }

    // ------------------------------------------
    // RESOLVING EVENTS PHASE
    // ------------------------------------------
    public ActionResult resolveEvents() {
        List<EventCard> events = cardMarket.getBottomRowEvents();

        cleanUp();

        // Progressione round e fine partita
        if (currentRound == 10) {
            List<EventCard> topEvents = cardMarket.getTopRowEvents();
            events.addAll(topEvents);
            eventResolver.resolveEvents(events, this);
            // Trigger ENDGAME: Calcolo punteggi
            phase = GamePhase.ENDED;
            gameState = buildGameState();
            notifyGameOver();
        } else {
            eventResolver.resolveEvents(events, this);
            currentRound++;
            phase = GamePhase.PLACING_TOTEMS;
            gameState = buildGameState();
            notifyEndOfResolvingPhase();
        }

        notifyGameStateChanged();
        return new ActionResult(gameState);
    }

    // ------------------------------------------
    // CLEANUP PHASE (END OF ROUND) + ERA PROGRESSION: Preparo la board al prossimo round e gestisco cambio era
    // ------------------------------------------
    private void cleanUp() {
        cardMarket.clearBottomRow();
        cardMarket.moveTopRowToBottom();
        RefillResult result = cardMarket.refillTopRow(tribeDeck, players.size(), currentEra);

        if (result.isEraAdvanced()) {
            Era newEra = result.getNewEra();
            cardMarket.handleEraProgression(buildingDeck, newEra);
            currentEra = newEra;
            notifyEraProgression();
        }
    }

    // ------------------------------------------
    // ENDGAME PHASE: Calcolo punteggi
    // ------------------------------------------
    public ActionResult calculateScores() {
        ScoreCalculator scoreCalculator = new ScoreCalculator(this);
        List<ScoreResult> scoreBoard = scoreCalculator.calculateFinalScores();
        // TODO: mettere scoreBoard nell'oggetto di ritorno

        gameState = buildGameState();
        notifyScores();
        return new ActionResult(gameState);
    }

    // ------------------------------------------
    // EXTRA DRAW: Alla fine del DRAWING PHASE, il player salvato in pendingExtraDrawPlayerId può pescare
    // ------------------------------------------
    // Chiamata dal controller quando il player ha il building con l'effetto extra card
    @Override
    public ActionResult takeExtraCard(String playerId, int index) {
        // TODO: gestire il giocatore che pesca una carta extra


        phase = GamePhase.RESOLVING_EVENTS;
        gameState = buildGameState();
        clearPendingExtraDrawPlayer();
        return new ActionResult(gameState);
    }

    public void setPendingExtraDrawPlayerId(String playerId) {
        this.pendingExtraDrawPlayerId = playerId;
    }

    public void clearPendingExtraDrawPlayer() {
        this.pendingExtraDrawPlayerId = null;
    }


    // ------------------------------------------
    // EVENTS FUNCTIONS
    // ------------------------------------------
    // Sustenance Event
    public void applyFoodCostWithPointsFallback(Player player, int cost) {
        if (player.getFood() >= cost) {
            player.spendFood(cost);
        } else {
            player.spendPrestigePoints(cost * currentEra.ordinal());
        }
    }


    // ------------------------------------------
    // GAMESTATE
    // ------------------------------------------
    private GameState buildGameState() {
        // TODO: modellare GameState
        return new GameState();
    }


    // ------------------------------------------
    // OBSERVERS e NOTIFICHE
    // ------------------------------------------
    public void addObserver(ModelObserver o) {
        observers.add(o);
    }

    public void removeObserver(ModelObserver o) {
        observers.remove(o);
    }

    // NOTIFICHE

    private void notifyGameStarted() {
        for (ModelObserver o : observers) o.onGameStarted();
    }

    private void notifyGameStateChanged() {
        for (ModelObserver o : observers) o.onGameStateChanged(gameState);
    }

    // Notifica che tutti i totem sono stati piazzati
    private void notifyEndOfPlacingPhase() {
        for (ModelObserver o : observers) o.onEndOfPlacingPhase(gameState);
    }

    // Notifica che tutti i giocatori hanno pescato e sono ritornati al Turn Order Tile
    private void notifyEndOfDrawingPhase() {
        for (ModelObserver o : observers) o.onEndOfDrawingPhase(gameState);
    }

    // Notifica che un player deve pescare una carta in più al termina della DRAWING PHASE
    private void notifyExtraDrawRequest() {
        for (ModelObserver o : observers) o.onExtraDrawRequest(gameState);
    }

    // Notifica che tutti gli eventi sono stati risolti
    private void notifyEndOfResolvingPhase() {
        for (ModelObserver o : observers) o.onEndOfResolvingPhase(gameState);
    }

    // Notifica cambio era
    private void notifyEraProgression() {
        for (ModelObserver o : observers) o.onEraProgression(gameState);
    }

    // Game over
    private void notifyGameOver() {
        for (ModelObserver o : observers) o.onGameOver();
    }

    // Notifica punteggi finali
    private void notifyScores() {
        for (ModelObserver o : observers) o.onScoreboardAvailable();
    }

    // GETTERS

    @Override
    public GameState getGameState() {
        return gameState;
    }

    @Override
    public GamePhase getGamePhase() {
        return phase;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Board getBoard() {
        return board;
    }

    public Era getCurrentEra() {
        return currentEra;
    }

    public int getCurrentRound() {
        return currentRound;
    }

}
