package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.exceptions.*;               // tutte le exceptions custom
import it.polimi.ingsw.am23.model.board.*;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.cards.SelectedCards;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.deck.BuildingDeck;
import it.polimi.ingsw.am23.model.deck.TribeDeck;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.resolvers.EventResolver;
import it.polimi.ingsw.am23.model.resolvers.ScoreCalculator;
import it.polimi.ingsw.am23.model.state.GameState;

import java.util.*;

public class Game implements GameModel {

    // Attributi
    private List<Player> players;
    private Board board;
    private TribeDeck tribeDeck;
    private BuildingDeck buildingDeck;
    private RoundManager roundManager;
    private EventResolver eventResolver;
    private ScoreCalculator scoreCalculator;
    private CardMarket cardMarket;
    private List<ModelObserver> observers = new ArrayList<>();

    // Stato partita
    private GameState gameState;
    private Era currentEra;
    private int currentRound;
    private GamePhase phase;
    private String pendingExtraDrawPlayerId;

    // Risoluzione
    private String currentResolvingPlayerId;
    private int remainingTotems;
    private int remainingBottomDraws;
    private boolean buildingBuiltInCurrentResolution;
    private boolean currentResolutionInitialized;

    public Game(List<Player> players, Board board, TribeDeck tribeDeck, BuildingDeck buildingDeck, RoundManager roundManager, EventResolver eventResolver, CardMarket cardMarket, Era currentEra, int currentRound) {
        this.players = players;
        this.board = board;
        this.tribeDeck = tribeDeck;
        this.buildingDeck = buildingDeck;
        this.roundManager = roundManager;
        this.eventResolver = eventResolver;
        this.cardMarket = cardMarket;
        this.currentEra = currentEra;
        this.currentRound = currentRound;
    }

    // PUBBLICI

    // Setup completato, comunico al controller setup terminato
    public void startGame() {
        notifyGameStarted();
    }

    public Player findPlayer(String playerId) {
        return players.stream()
                .filter(p -> Objects.equals(p.getId(), playerId))
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException("Player " + playerId + " not found in this match"));
    }

    @Override
    public ActionResult placeTotem(String playerId, char offerTileChar) {
        Player p = findPlayer(playerId);
        // Verifico che sia lui il prossimo a dover piazzare il totem
        if (!Objects.equals(board.getTurnOrderTile().getFirstFreeSlot().getPlayerId(), p.getId())) {
            // TODO: ActionResult + Errore
            throw new IllegalActionException("It's not your turn bastard.");
        }

        // Verifico che la tile sia vuota
        OfferTile tile = board.getOfferTile(offerTileChar);
        if (!tile.isFree()) {
            // TODO: ActionResult + Errore
            throw new TileNotEmptyException("The selected offer tile is not empty");
        }
        // Rimuovo il totem dalla tessera ordine di turno
        board.getTurnOrderTile().getFirstFreeSlot().clear();
        // E lo piazzo nella offer tile selezionata
        tile.placeTotem(p.getId());

//        // Azioni della tessera tracciato delle offerte
//        OfferAction actions = tile.getAction();
//        p.addFood(actions.getFoodReward());         // food reward

        // Se tutti hanno piazzato, la tessera ordine di turno é vuota, notifico il controller
        if (board.getTurnOrderTile().isEmpty()) {
            notifyEndOfPlacingPhase();
        }

        gameState = buildGameState();
        notifyGameStateChanged();
        return new ActionResult(gameState);
    }

    @Override
    public ActionResult takeCards(String playerId, SelectedCards selectedCards) {
        Player p = findPlayer(playerId);
        // Verifico che sia lui il prossimo a poter pescare



        Card card = cardMarket.getCard(row, index);
        // Verifico sia prendibile
        if (!card.canBeTaken()) {
            throw new CardNotDrawableException("This card cannot be drawn from the card market");
        }
        // Se edifico, verifico che il player abbia abbastanza cibo
        if ((card instanceof BuildingCard) && (((BuildingCard) card).getFoodCost() > p.getFood())) {
            throw new CardNotDrawableException("The food cost exceeds the player's reserve");
        }

        // Azioni e aggiunta della carta
        if (card instanceof CharacterCard) {
            card.onTaken(this, p);
            gameState = buildGameState();
            notifyGameStateChanged();
        } else if (card instanceof BuildingCard) {
            p.spendFood(((BuildingCard) card).getFoodCost());
            card.onTaken(this, p);
            gameState = buildGameState();
            notifyGameStateChanged();
        }
        return new ActionResult(gameState);
    }

    // Chiamata dal controller quando il player ha il building con l'effetto extra card
    @Override
    public ActionResult takeExtraCard(String playerId, int index) {
        return new ActionResult(gameState);
    }

    public void setPendingExtraDrawPlayerId(String playerId) {
        this.pendingExtraDrawPlayerId = playerId;
    }

    public void clearPendingExtraDrawPlayer() {
        this.pendingExtraDrawPlayerId = null;
    }

    public void applyFoodCostWithPointsFallback(Player player, int cost) {
        // TODO: player deve pagare il costo "cost", con fallback su perdita di punti se non ha abbastanza cibo
        // TODO: + traduzione cibo-punti dinamica in base all'era
    }

    // PRIVATI

    // Gestione fasi/round
    private void prepareResolvingPhase() {
    }

    private void triggerAfterAllActionsEffects() {
    }

    private void startEventPhaseAndFinishRound() {
    }

    private boolean isGameEndedAfterRound() {
        return false; //temporaneo per errori IDE
    }

    // Gestione turn order / board
    private void returnTotemToTile(Player player) {
    }

    private void applyTurnOrderEffect(Player player, TurnOrderSlot slot) {
    }

    private GameState buildGameState() {
        return new GameState(
                currentEra,
                currentRound,
                phase,
                currentResolvingPlayerId,
                players.stream().map(Player.getState()).toList(),
                board.getState(),
                pendingExtraDrawPlayerId
        );
    }

    // Observers e notifiche
    public void addObserver(ModelObserver o) {
        observers.add(o);
    }

    public void removeObserver(ModelObserver o) {
        observers.remove(o);
    }

    private void notifyGameStarted() {
        for (ModelObserver o : observers) o.onGameStarted();
    }

    private void notifyGameStateChanged() {
        for (ModelObserver o : observers) o.onGameStateChanged(gameState);
    }

    private void notifyGameOver() {
        for (ModelObserver o : observers) o.onGameOver();
    }

    // Notifica che tutti i totem sono stati piazzati
    private void notifyEndOfPlacingPhase() {
        for (ModelObserver o : observers) o.onEndOfPlacingPhase(gameState);
    }


    // Getters

    @Override
    public GameState getGameState() {
        return gameState;
    }

    @Override
    public GamePhase getGamePhase() {
        return phase;
    }

    @Override
    public String getCurrentPlayerId() {
        return currentResolvingPlayerId;
    }

    public String getCurrentResolvingPlayerId() {   // TODO: capire se si intende questo con "current player id"
        return currentResolvingPlayerId;
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
