package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.exceptions.IllegalActionException;
import it.polimi.ingsw.am23.exceptions.PlayerNotFoundException;
import it.polimi.ingsw.am23.model.board.Board;
import it.polimi.ingsw.am23.model.board.CardMarket;
import it.polimi.ingsw.am23.model.board.OfferTile;
import it.polimi.ingsw.am23.model.board.RefillResult;
import it.polimi.ingsw.am23.model.cards.*;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderTile;
import it.polimi.ingsw.am23.model.deck.BuildingDeck;
import it.polimi.ingsw.am23.model.deck.TribeDeck;
import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.enums.RowType;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.resolvers.EventResolver;
import it.polimi.ingsw.am23.model.resolvers.ScoreCalculator;
import it.polimi.ingsw.am23.model.resolvers.ScoreResult;
import it.polimi.ingsw.am23.model.state.GameState;
import it.polimi.ingsw.am23.model.state.ScoreEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private int upperPickedCount = 0;
    private int lowerPickedCount = 0;
    private int upperMaxCount = 0;
    private int lowerMaxCount = 0;
    private boolean drawingStarted = false;

    // TODO : fare moduli per conteggio pescaggio carte

     private String currentPlayerId = null;

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
        this.gameState = buildGameState();
    }


    // Setup completato, comunico al controller setup terminato
    public void startGame() {
        phase = GamePhase.PLACING_TOTEMS;
        gameState = buildGameState();
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
        if (!Objects.equals(board.getTurnOrderTile().getFirstOccupiedSlot().getPlayerId(), p.getId())) {
            return ActionResult.failure(ActionType.PLACE_TOTEM, ErrorCode.WRONG_PLAYER, "It's not your turn.");
        }

        // Verifico che la tile sia vuota
        OfferTile tile = board.getOfferTile(offerTileChar);
        if (!tile.isFree()) {
            return ActionResult.failure(ActionType.PLACE_TOTEM, ErrorCode.INVALID_TILE, "The selected offer tile is not empty.");
        }

        // Salvo ID del player che sta piazzando (per costruzione GameState)
        currentPlayerId = playerId;
        // Rimuovo il totem dalla tessera ordine di turno
        board.findTurnOrderSlotOccupiedBy(playerId).clear();
        // E lo piazzo nella offer tile selezionata
        tile.placeTotem(p.getId());

        gameState = buildGameState();
        notifyGameStateChanged();

        // Se tutti hanno piazzato, la tessera ordine di turno é vuota, notifico il controller
        if (board.getTurnOrderTile().isEmpty()) {
            this.phase = GamePhase.RESOLVING_OFFERS;
            currentPlayerId = null; // Reset del ID salvato
            gameState = buildGameState();
            notifyEndOfPlacingPhase();
        }else{ // non tutti hanno ancora pizzato, aggiorno solo lo stato
            gameState = buildGameState();
            notifyGameStateChanged();
        }

        return ActionResult.success(ActionType.PLACE_TOTEM, "Totem placed successfully");
    }

    // ------------------------------------------
    // DRAWING PHASE
    // ------------------------------------------

    @Override
    public ActionResult takeSingleCard(String playerId, SelectedSingleCard selectedSingleCard) {
        Player p = findPlayer(playerId);
        OfferTile tile = board.getFirstOccupiedOfferTile();

        if(!Objects.equals(p.getId(), tile.getOccupiedByPlayerId()))
            return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.WRONG_PLAYER, "It's not your turn.");

        // Inizializzazione turno se è la prima carta
        if(!drawingStarted){
            p.addFood(tile.getAction().getFoodReward());
            upperMaxCount = Math.min(
                    tile.getAction().getUpperDrawRowCount(),
                    cardMarket.getDrawableCount(RowType.TOP));
            lowerMaxCount = Math.min(
                    tile.getAction().getBottomDrawCount(),
                    cardMarket.getDrawableCount(RowType.BOTTOM));
            upperPickedCount = 0;
            lowerPickedCount = 0;
            drawingStarted = true;
            currentPlayerId = playerId;
        }

        // Verifico che il player possa ancora pescare da questa riga
        if (selectedSingleCard.getRow() == RowType.TOP && upperPickedCount >= upperMaxCount)
            return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.INVALID_ROW, "You already picked the maximun from the top row.");
        if (selectedSingleCard.getRow() == RowType.BOTTOM && lowerPickedCount >= lowerMaxCount)
            return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.INVALID_ROW, "You already picked the maximun from the bottom row.");

        int foodDiscount = p.getTribe().getBuildingDiscount();

        // Pesco la carta
        if (selectedSingleCard.isBuilding()){
            BuildingCard c = cardMarket.getBuilding(selectedSingleCard.getRow(), selectedSingleCard.getBoardIndex());
            if (c.getFoodCost() - foodDiscount > p.getFood())
                return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.NOT_ENOUGH_FOOD, "Not enough food.");
            cardMarket.removeBuilding(selectedSingleCard.getRow(), selectedSingleCard.getBoardIndex());
            c.onTaken(this, p);
            c.getEffect().onBuildingAdded(p);
            c.getEffect().onAfterAllActions(this, p);
            p.spendFood(c.getFoodCost() - foodDiscount);
        }else{
            Card c = cardMarket.getCard(selectedSingleCard.getRow(), selectedSingleCard.getBoardIndex());
            if (!c.canBeTaken())
                return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.CARD_NOT_TAKABLE, "This card cannot be drawn.");
            cardMarket.removeCard(selectedSingleCard.getRow(), selectedSingleCard.getBoardIndex());
            c.onTaken(this, p);
            // controllo se ti attiva qualche building effect
            for(BuildingCard building : p.getTribe().getBuildings()){
                building.getEffect().onCardTaken(this, p, c);
            }
        }

        // aggiorno i contatori
        if (selectedSingleCard.getRow() == RowType.TOP)
            upperPickedCount++;
        else
            lowerPickedCount++;

        // notifico la view dopo ogni carta pescata
        gameState = buildGameState();
        notifyGameStateChanged();

        // Verifico se il turno è concluso - se ha pescato tutte le carte che doveva
        if (upperPickedCount == upperMaxCount && lowerPickedCount == lowerMaxCount){
            drawingStarted = false;
            upperPickedCount = 0;
            lowerPickedCount = 0;
            returnToTurnOrder(playerId);
            currentPlayerId = null;

            if(board.getFirstOccupiedOfferTile() == null){
                phase = GamePhase.RESOLVING_EVENTS;
                gameState = buildGameState();
                if(pendingExtraDrawPlayerId != null){
                    phase = GamePhase.EXTRA_DRAW;
                    gameState = buildGameState();
                    notifyExtraDrawRequest();
                }else{
                    notifyEndOfDrawingPhase();
                }
            }else{
                gameState = buildGameState();
                notifyGameStateChanged();
            }
        }

        return ActionResult.success(ActionType.TAKE_CARD, "Card taken successfully.");
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
            // effetto building per ritorno su tessera con bonus cibo positivo
            for(BuildingCard building : findPlayer(playerId).getTribe().getBuildings()) {
                building.getEffect().modifyTurnOrderFood(this, findPlayer(playerId), slot.getFoodDelta());
            }
        } else if (slot.getFoodDelta() != 0) {                      // Delta negativo
            if (findPlayer(playerId).canAfford(slot.getFoodDelta())) {
                findPlayer(playerId).spendFood(slot.getFoodDelta());
            }else{
                findPlayer(playerId).spendPrestigePoints(slot.getFoodDelta() * 2);
            }
        }
    }


    // ------------------------------------------
    // RESOLVING EVENTS PHASE
    // ------------------------------------------
    @Override
    public ActionResult resolveEvents() {
        List<EventCard> events = cardMarket.getBottomRowEvents();

        if (currentRound == 10) {
            List<EventCard> topEvents = cardMarket.getTopRowEvents();
            events.addAll(topEvents);
            for(EventCard event : events){
                eventResolver.resolveSingleEvent(event, this);
                gameState = buildGameState();
                notifyEventResolved();
            }
            cleanUp();
            phase = GamePhase.ENDED;
            gameState = buildGameState();
            notifyGameOver();
        } else {
            for(EventCard event : events){
                eventResolver.resolveSingleEvent(event, this);
                gameState = buildGameState();
                notifyEventResolved();
            }
            cleanUp();
            currentRound++;
            phase = GamePhase.PLACING_TOTEMS;
            gameState = buildGameState();
            notifyGameStateChanged();
        }

        return ActionResult.success(ActionType.END_ROUND, "Events resolved successfully");
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
            gameState = buildGameState();
            notifyEraProgression();
        }
    }

    // ------------------------------------------
    // ENDGAME PHASE: Calcolo punteggi
    // ------------------------------------------
    @Override
    public ActionResult calculateScores() {
        ScoreCalculator scoreCalculator = new ScoreCalculator(this);
        List<ScoreResult> scoreBoard = scoreCalculator.calculateFinalScores();
        gameState = buildGameStateWithScores(scoreBoard);
        notifyScores();
        return ActionResult.success(ActionType.GENERIC, "Scores calculated successfully");
    }

    // ------------------------------------------
    // EXTRA DRAW: Alla fine del DRAWING PHASE, il player salvato in pendingExtraDrawPlayerId può pescare
    // ------------------------------------------
    // Chiamata dal controller quando il player ha il building con l'effetto extra card
    @Override
    public ActionResult takeExtraCard(String playerId, SelectedCardExtraDraw selectedCardExtraDraw) {
        Player p = findPlayer(playerId);

        // Se non ci sono player salvati per l'extra draw dò errore
        if (pendingExtraDrawPlayerId == null) {
            return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.NO_PENDING_EXTRA_DRAW, "There are no pending extra draws.");
        }

        // Verifico sia il player corretto per fare l'extra draw
        if (!Objects.equals(p.getId(), pendingExtraDrawPlayerId)) {
            return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.INVALID_EXTRA_DRAW, "It's not your turn.");
        }

        // Aggiungo le carte alla tribù del Player
        int foodDiscount = p.getTribe().getBuildingDiscount();
        // Distinzione tribe card - building card

        if (selectedCardExtraDraw.isTribeCard()) {
            int boardIndex = selectedCardExtraDraw.getCardIndex();
            Card c = cardMarket.getCard(RowType.TOP, boardIndex);
            // Verifico sia prendibile
            if (!c.canBeTaken()) {
                return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.INVALID_EXTRA_DRAW, "This card cannot be drawn from the card market.");
            }
            cardMarket.removeCard(RowType.TOP, boardIndex);
            c.onTaken(this, p);
            for (BuildingCard building : p.getTribe().getBuildings()) {
                building.getEffect().onCardTaken(this, p, c);
            }
        } else {
            int boardIndex = selectedCardExtraDraw.getBuildingIndex();
            BuildingCard c = cardMarket.getBuilding(RowType.TOP, boardIndex);
            // Verifico sia prendibile
            // Verifico costo
            if (c.getFoodCost() - foodDiscount > p.getFood()) {
                return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.NOT_ENOUGH_FOOD, "The food cost exceeds the player's reserve.");
            }
            cardMarket.removeBuilding(RowType.TOP, boardIndex);
            c.onTaken(this, p);
            c.getEffect().onBuildingAdded(p);
            c.getEffect().onAfterAllActions(this, p);
            p.spendFood(c.getFoodCost() - foodDiscount);
        }

        phase = GamePhase.RESOLVING_EVENTS;
        clearPendingExtraDrawPlayer();
        gameState = buildGameState();
        notifyGameStateChanged();

        return ActionResult.success(ActionType.TAKE_CARD, "Extra card taken successfully");
    }

    public void setPendingExtraDrawPlayerId(String playerId) {
        this.pendingExtraDrawPlayerId = playerId;
    }

    public void clearPendingExtraDrawPlayer() {
        this.pendingExtraDrawPlayerId = null;
    }


    // ------------------------------------------
    // GAMESTATE
    // ------------------------------------------
    private GameState buildGameState() {
        return new GameState(
                currentEra,
                currentRound,
                phase,
                computeCurrentPlayerId(),
                players.stream().map(Player::getState).toList(),
                board.getState(cardMarket)
        );
    }

    private GameState buildGameStateWithScores(List<ScoreResult> scoreBoard){
        List<ScoreEntry> scores = scoreBoard.stream()
                .map(r-> new ScoreEntry(r.player.getId(), r.foodPoints, r.PP))
                .toList();

        return new GameState(
                currentEra,
                currentRound,
                phase,
                computeCurrentPlayerId(),
                players.stream().map(Player::getState).toList(),
                board.getState(cardMarket),
                scores
        );
    }

    private String computeCurrentPlayerId() {
        if (phase == GamePhase.EXTRA_DRAW) {
            return pendingExtraDrawPlayerId;
        }
        return currentPlayerId;
    }


    // ------------------------------------------
    // OBSERVERS e NOTIFICHE
    // ------------------------------------------
    @Override
    public void addObserver(ModelObserver o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(ModelObserver o) {
        observers.remove(o);
    }

    // NOTIFICHE

    private void notifyGameStarted() {
        observers.forEach(o->o.onGameStarted(gameState));
    }

    private void notifyGameStateChanged() {
        observers.forEach(o-> o.onGameStateChanged(gameState));
    }

    private void notifyEndOfPlacingPhase(){
        observers.forEach(o->o.onEndOfPlacingPhase(gameState));
    }

    private void notifyEndOfDrawingPhase(){
        observers.forEach(o->o.onEndOfDrawingPhase(gameState));
    }

    private void notifyExtraDrawRequest() {
        observers.forEach(o->o.onExtraDrawRequest(gameState));
    }

    private void notifyEventResolved() {
        observers.forEach(o->o.onEventResolved(gameState));
    }

    private void notifyEraProgression() {
        observers.forEach(o->o.onEraProgression(gameState));
    }

    private void notifyGameOver() {
        observers.forEach(o->o.onGameOver(gameState));
    }

    private void notifyScores() {
        observers.forEach(o->o.onScoreboardAvailable(gameState));
    }


    // GETTERS
    public List<Player> getPlayers() {
        return players;
    }

    public Board getBoard() {
        return board;
    }

    public TribeDeck getTribeDeck() {
        return tribeDeck;
    }

    public BuildingDeck getBuildingDeck() {
        return buildingDeck;
    }

    public EventResolver getEventResolver() {
        return eventResolver;
    }

    public CardMarket getCardMarket() {
        return cardMarket;
    }

    public List<ModelObserver> getObservers() {
        return observers;
    }

    @Override
    public GameState getGameState() {
        return gameState;
    }

    @Override
    public GamePhase getGamePhase() {
        return phase;
    }

    public Era getCurrentEra() {
        return currentEra;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public String getPendingExtraDrawPlayerId() {
        return pendingExtraDrawPlayerId;
    }

    public String getCurrentPlayerId() {
        return currentPlayerId;
    }
}