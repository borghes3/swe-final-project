package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.exceptions.*;               // tutte le exceptions custom
import it.polimi.ingsw.am23.model.board.*;
import it.polimi.ingsw.am23.model.cards.*;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderTile;
import it.polimi.ingsw.am23.model.deck.BuildingDeck;
import it.polimi.ingsw.am23.model.deck.TribeDeck;
import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.enums.RowType;
import it.polimi.ingsw.am23.model.payloads.*;
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

     private String currentPlayerId = null;
    // private List<ScoreResult> scoreBoard;
    // fatto in aula il 14 aprile, ma con i payloads non dovrebbe servire

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

        notifyTotemPlaced(playerId, offerTileChar);

        // Se tutti hanno piazzato, la tessera ordine di turno é vuota, notifico il controller
        if (board.getTurnOrderTile().isEmpty()) {
            this.phase = GamePhase.RESOLVING_OFFERS;
            gameState = buildGameState();
            notifyEndOfPlacingPhase();
            currentPlayerId = null; // Reset del ID salvato
        }

        gameState = buildGameState();
        return ActionResult.success(ActionType.PLACE_TOTEM, "Totem placed successfully");
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
            return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.WRONG_PLAYER, "It's not your turn.");
        }

        // Verifico che il numero di carte richieste sia corretto
        if (!checkDrawingCriteria(tile, selectedCards)) {
            throw new IllegalActionException("Invalid number of cards.");
        }

        // Salvo ID del player che sta piazzando (per costruzione GameState)
        currentPlayerId = playerId;

        // Food reward se presente sul Offer Tile
        int foodGainedFromTile = tile.getAction().getFoodReward();
        p.addFood(tile.getAction().getFoodReward());

        int foodDiscount = p.getTribe().getBuildingDiscount();
        int foodSpentOnBuildings = 0;

        List<String> takenCardIds = new ArrayList<>();
        List<String> takenBuildingIds = new ArrayList<>();

        // LOWER ROW
        // Tribe Cards
        for (int boardIndex : selectedCards.getLowerRow()) {
            Card c = cardMarket.getCard(RowType.BOTTOM, boardIndex);
            // Verifico sia prendibile
            if (!c.canBeTaken()) {
                return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.CARD_NOT_TAKABLE, "This card cannot be drawn from the card market.");
            }
            // Aggiungo alla tribe e rimuovo dal market
            cardMarket.removeCard(RowType.BOTTOM, boardIndex);
            c.onTaken(this, p);
            for (BuildingCard building : p.getTribe().getBuildings()) {
                building.getEffect().onCardTaken(this, p, c);
            }
            // come si vuole notificare in questo caso?
            takenCardIds.add(c.getId());
        }
        // Buildings
        for (int boardIndex : selectedCards.getLowerBuildings()) {
            BuildingCard c = cardMarket.getBuilding(RowType.BOTTOM, boardIndex);
            // Verifico costo
            if (c.getFoodCost() - foodDiscount > p.getFood()) {
                return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.NOT_ENOUGH_FOOD, "The food cost exceeds the player's reserve.");
            }
            // Aggiungo alla tribe e rimuovo cibo dal player
            cardMarket.removeBuilding(RowType.BOTTOM, boardIndex);
            c.onTaken(this, p);  // aggiungo alla tribe
            c.getEffect().onBuildingAdded(p);  // chiamo effetti di inizializzazione per building
            c.getEffect().onAfterAllActions(this, p);
            p.spendFood(c.getFoodCost() - foodDiscount);
            foodSpentOnBuildings += (c.getFoodCost() - foodDiscount);
            takenBuildingIds.add(c.getId());
        }

        // UPPER ROW
        // Tribe Cards
        for (int boardIndex : selectedCards.getUpperRow()) {
            Card c = cardMarket.getCard(RowType.TOP, boardIndex);
            // Verifico sia prendibile
            if (!c.canBeTaken()) {
                return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.CARD_NOT_TAKABLE, "This card cannot be drawn from the card market.");
            }
            // Aggiungo alla tribe e rimuovo dal market
            cardMarket.removeCard(RowType.TOP, boardIndex);
            c.onTaken(this, p);
            for (BuildingCard building : p.getTribe().getBuildings()) {
                building.getEffect().onCardTaken(this, p, c);
            }
            takenCardIds.add(c.getId());
        }
        // Buildings
        for (int boardIndex : selectedCards.getUpperBuildings()) {
            BuildingCard c = cardMarket.getBuilding(RowType.TOP, boardIndex);
            // Verifico costo
            if (c.getFoodCost() - foodDiscount > p.getFood()) {
                return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.NOT_ENOUGH_FOOD, "The food cost exceeds the player's reserve.");
            }
            // Aggiungo alla tribe e rimuovo cibo dal player
            cardMarket.removeBuilding(RowType.TOP, boardIndex);
            c.onTaken(this, p);
            c.getEffect().onBuildingAdded(p);
            c.getEffect().onAfterAllActions(this, p);
            p.spendFood(c.getFoodCost() - foodDiscount);
            foodSpentOnBuildings += (c.getFoodCost() - foodDiscount);
            takenBuildingIds.add(c.getId());
        }

        // Ritorno al turn order
        int[] slotResult = returnToTurnOrder(playerId);
        int slotIndex = slotResult[0];
        int foodDeltaFromSlot = slotResult[1];

        notifyCardsTaken(playerId, takenCardIds, takenBuildingIds, foodSpentOnBuildings, foodGainedFromTile, slotIndex, foodDeltaFromSlot);

        // se tutti hanno pescato, il tracciato è vuoto, notifico il controller
        if (board.getFirstOccupiedOfferTile() == null) {
            phase = GamePhase.RESOLVING_EVENTS;
            gameState = buildGameState();
            currentPlayerId = null; // Reset del ID salvato
            // Se è stato settato un player che deve fare extra draw, lo notifico ora
            if (pendingExtraDrawPlayerId != null) {
                phase = GamePhase.EXTRA_DRAW;
                gameState = buildGameState();
                notifyExtraDrawRequest();
            }
        }

        gameState = buildGameState();
        return ActionResult.success(ActionType.TAKE_CARD, "Cards taken successfully");
    }

    // verifica condizioni del payload delle carte da pescare selezionate
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
    private int[] returnToTurnOrder(String playerId) {
        // Rimuovo il player dal Offer Tile in cui si trova
        OfferTile offerTile = board.getOfferTileByPlayerId(playerId);
        offerTile.clear();
        // Recupero primo slot libero e posiziono il player
        TurnOrderTile turnTile = board.getTurnOrderTile();
        TurnOrderSlot slot = turnTile.getFirstFreeSlot();
        slot.placeTotem(playerId);

        List<TurnOrderSlot> slots = turnTile.getSlots();
        int slotIndex = slots.indexOf(slot);

        int foodDelta=0;
        // Gestione delta cibo del turn order slot
        if (slot.givesFood()) {                                     // Delta positivo
            findPlayer(playerId).addFood(slot.getFoodDelta());
            // effetto building per ritorno su tessera con bonus cibo positivo
            for(BuildingCard building : findPlayer(playerId).getTribe().getBuildings()) {
                building.getEffect().modifyTurnOrderFood(this, findPlayer(playerId), slot.getFoodDelta());
            }
            foodDelta = slot.getFoodDelta();
        } else if (slot.getFoodDelta() != 0) {                      // Delta negativo
            if (findPlayer(playerId).canAfford(slot.getFoodDelta())) {
                findPlayer(playerId).spendFood(slot.getFoodDelta());
                foodDelta = -slot.getFoodDelta();
            }else{
                findPlayer(playerId).spendPrestigePoints(slot.getFoodDelta() * 2);
                foodDelta=0; // ha pagato con PP e non con cibo
            }
        }
        return new int[]{slot.getIndex(), foodDelta};
    }

    public boolean isDrawingPhaseOver(){
        return board.getFirstOccupiedOfferTile() == null;
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
            resolveAndNotifyEvents(events);
            cleanUp();

            phase = GamePhase.ENDED;
            gameState = buildGameState();
            notifyGameOver();
        } else {
            resolveAndNotifyEvents(events);
            cleanUp();
            currentRound++;
            phase = GamePhase.PLACING_TOTEMS;
            gameState = buildGameState();
        }

        return ActionResult.success(ActionType.END_ROUND, "Events resolved successfully");
    }

    // risolve evnti e notifica a ciascuno separatamente
    private void resolveAndNotifyEvents(List<EventCard> events){
        for(EventCard event : events){
            List<PlayerDelta> deltasBefore = snapshotDeltas();
            eventResolver.resolveSingleEvent(event, this);
            List<PlayerDelta> deltasAfter = computeDeltas(deltasBefore);
            notifyEventResolved(event.getId(), currentEra, deltasAfter);
        }
    }

    // snapshot di cibo e PP di tutti i player prima dell'evento
    private List<PlayerDelta> snapshotDeltas() {
        return players.stream()
                .map(p -> new PlayerDelta(p.getId(), p.getFood(), p.getPrestigePoints()))
                .toList();
    }

    // calcola i delta confrontando snapshot prima/dopo
    private List<PlayerDelta> computeDeltas(List<PlayerDelta> before) {
        return players.stream().map(p -> {
            PlayerDelta old = before.stream()
                    .filter(d -> d.playerId().equals(p.getId()))
                    .findFirst().orElseThrow();
            return new PlayerDelta(
                    p.getId(),
                    p.getFood() - old.foodDelta(),
                    p.getPrestigePoints() - old.prestigeDelta()
            );
        }).toList();
    }

    // ------------------------------------------
    // CLEANUP PHASE (END OF ROUND) + ERA PROGRESSION: Preparo la board al prossimo round e gestisco cambio era
    // ------------------------------------------
    private void cleanUp() {
        /*cardMarket.clearBottomRow();
        cardMarket.moveTopRowToBottom();
        RefillResult result = cardMarket.refillTopRow(tribeDeck, players.size(), currentEra);

        if (result.isEraAdvanced()) {
            Era newEra = result.getNewEra();
            cardMarket.handleEraProgression(buildingDeck, newEra);
            currentEra = newEra;
        }*/
        //vecchio metodo, da riprendere se il nuovo non funziona

        List<String> discardedCards = cardMarket.getBottomRow().stream()
                .map(Card::getId).toList();
        List<String> movedCards = cardMarket.getTopRow().stream()
                .map(Card::getId).toList();

        cardMarket.clearBottomRow();
        cardMarket.moveTopRowToBottom();

        RefillResult result = cardMarket.refillTopRow(tribeDeck, players.size(), currentEra);
        List<String> newUpperRow = cardMarket.getTopRow().stream()
                .map(Card::getId).toList();

        notifyMarketRefreshed(discardedCards, movedCards, newUpperRow);

        if (result.isEraAdvanced()) {
            Era newEra = result.getNewEra();

            // Raccolgo i discarded PRIMA di handleEraProgression (dopo non esistono più)
            List<String> discardedBuildings = (newEra == Era.ERA_3)
                    ? cardMarket.getBottomBuildings().stream()
                    .map(BuildingCard::getId).toList()
                    : List.of();

            cardMarket.handleEraProgression(buildingDeck, newEra);
            currentEra = newEra;

            List<String> newBuildings = cardMarket.getTopBuildings().stream()
                    .map(BuildingCard::getId).toList();

            notifyEraProgression(newEra, newBuildings, discardedBuildings);
        }
    }

    // ------------------------------------------
    // ENDGAME PHASE: Calcolo punteggi
    // ------------------------------------------
    @Override
    public ActionResult calculateScores() {
        ScoreCalculator scoreCalculator = new ScoreCalculator(this);
        List<ScoreResult> scoreBoard = scoreCalculator.calculateFinalScores();

        List<PlayerScore> scores = scoreBoard.stream()
                .map(r->new PlayerScore(r.player.getId(),r.PP, Map.of()))
                .toList();

        gameState = buildGameState();
        notifyScores(scores);
        return ActionResult.success(ActionType.GENERIC, "Scores calculated successfully");
    }

    // ------------------------------------------
    // EXTRA DRAW: Alla fine del DRAWING PHASE, il player salvato in pendingExtraDrawPlayerId può pescare
    // ------------------------------------------
    // Chiamata dal controller quando il player ha il building con l'effetto extra card
    @Override
    public ActionResult takeExtraCard(String playerId, SelectedCardExtraDraw selectedCardExtraDraw) {
        // TODO: Gestire index della carta da pescare (da quale lista? Tribe o Buildings?)
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
        String cardId;
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
            cardId = c.getId();
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
            cardId = c.getId();
        }

        notifyExtraCardTaken(playerId, cardId);

        phase = GamePhase.RESOLVING_EVENTS;
        clearPendingExtraDrawPlayer();
        gameState = buildGameState();

        return ActionResult.success(ActionType.TAKE_CARD, "Extra card taken successfully");
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
    /* public void applyFoodCostWithPointsFallback(Player player, int cost) {
        if (player.getFood() >= cost) {
            player.spendFood(cost);
        } else {
            player.spendPrestigePoints(cost * currentEra.ordinal());
        }
    }
    */


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
        GameStartedPayload payload = new GameStartedPayload(gameState);
        observers.forEach(o->o.onGameStarted(payload));
    }

    //private void notifyGameStateChanged() {for (ModelObserver o : observers) o.onGameStateChanged(gameState);}

    private void notifyTotemPlaced(String playerId, char offerTileChar){
        TotemPlacedPayload payload = new TotemPlacedPayload(playerId, offerTileChar);
        observers.forEach(o->o.onTotemPlaced(payload));
    }

    // Notifica che tutti i totem sono stati piazzati
    private void notifyEndOfPlacingPhase() {
        List<String> order = board.getOfferTiles().stream()
                .filter(t -> !t.isFree())
                .map(OfferTile::getOccupiedByPlayerId)
                .toList();
        EndOfPlacingPhasePayload payload = new EndOfPlacingPhasePayload(order);
        observers.forEach(o->o.onEndOfPlacingPhase(payload));
    }

    private void notifyCardsTaken(String playerId, List<String> cardIds, List<String> buildingIds, int foodSpent, int foodGained, int slotIndex, int foodDeltaFromSlots){
        CardsTakenPayload payload = new CardsTakenPayload(playerId, cardIds, buildingIds, foodSpent, foodGained, slotIndex, foodDeltaFromSlots);
        observers.forEach(o->o.onCardsTaken(payload));
    }

    // Notifica che un player deve pescare una carta in più al termina della DRAWING PHASE
    private void notifyExtraDrawRequest() {
        ExtraDrawRequestPayload payload = new ExtraDrawRequestPayload(pendingExtraDrawPlayerId);
        observers.forEach(o->o.onExtraDrawRequest(payload));
    }

    private void notifyExtraCardTaken(String playerId, String cardId){
        ExtraCardTakenPayload payload = new ExtraCardTakenPayload(playerId, cardId);
        observers.forEach(o->o.onExtraCardTaken(payload));
    }

    private void notifyEventResolved(String eventCardId, Era era, List<PlayerDelta> deltas) {
        EventResolvedPayload payload = new EventResolvedPayload(eventCardId, era, deltas);
        observers.forEach(o->o.onEventResolved(payload));
    }

    // Notifica cambio era
    private void notifyEraProgression(Era newEra, List<String> newBuildings, List<String> discarded) {
        EraProgressionPayload payload = new EraProgressionPayload(newEra, newBuildings, discarded);
        observers.forEach(o->o.onEraProgression(payload));
    }

    private void notifyMarketRefreshed(List<String> discarded, List<String> moved, List<String> newUpper){
        MarketRefresherPayload payload = new MarketRefresherPayload(discarded, moved,newUpper);
        observers.forEach(o->o.onMarketRefreshed(payload));
    }

    // Game over
    private void notifyGameOver() {
        observers.forEach(o->o.onGameOver());
    }

    // Notifica punteggi finali
    private void notifyScores(List<PlayerScore> scores) {
        ScoreBoardPayload payload = new ScoreBoardPayload(scores);
        observers.forEach(o->o.onScoreboardAvailable(payload));
    /* private void notifyScores() {
        for (ModelObserver o : observers) o.onScores(scoreBoard);
    }*/
        // vecchio
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