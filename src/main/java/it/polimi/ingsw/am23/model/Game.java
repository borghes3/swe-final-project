package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.exceptions.*;               // tutte le exceptions custom
import it.polimi.ingsw.am23.model.board.*;
import it.polimi.ingsw.am23.model.cards.BuildingCard;
import it.polimi.ingsw.am23.model.cards.Card;
import it.polimi.ingsw.am23.model.cards.CharacterCard;
import it.polimi.ingsw.am23.model.deck.BuildingDeck;
import it.polimi.ingsw.am23.model.deck.TribeDeck;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.enums.RowType;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.resolvers.EventResolver;
import it.polimi.ingsw.am23.model.resolvers.ScoreCalculator;
import it.polimi.ingsw.am23.model.state.BoardState;
import it.polimi.ingsw.am23.model.state.GameState;
import it.polimi.ingsw.am23.model.state.PlayerState;

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

    // PUBBLICI
    public void startGame() {
        // TODO: logica di inizializzazione
        notifyGameStarted();
    }

    public Player findPlayer(String playerId) {
        return players.stream()
                .filter(p -> Objects.equals(p.getId(), playerId))
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException("Player " + playerId + " not found in this match"));
    }

    @Override
    public ActionResult placeTotem(String playerId, int offerTilePosition) {
        Player p = findPlayer(playerId);
        OfferTile tile = board.getOfferTile(offerTilePosition);
        // Verifico che la tile sia vuota
        if (!tile.isFree()) {
            throw new TileNotEmptyException("The selected offer tile is not empty");
        }
        tile.placeTotem(p.getId()); // non uso playerId, anche se precedentemente validato, non ci fidiamo del client
        // Azioni della tessera tracciato delle offerte
        OfferAction actions = tile.getAction();
        p.addFood(actions.getFoodReward());         // food reward
        // TODO: GESTIRE IL NUMERO DI CARTE PRESE FINORA E VERIFICARE SE PUÓ PRENDERNE ALTRE

        return new ActionResult();
        gameState = buildGameState();
        notifyGameStateChanged();
        return new ActionResult();  // TODO: cosa deve contenere ActionResult?
    }

    @Override
    public ActionResult takeCard(String playerId, RowType row, int index) {
        Player p = findPlayer(playerId);
        // Azioni da compiere per questa offer tile
        // TODO: le eseguiamo qui? in .placeTotem()? o in un metodo dedicato?

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
        return new ActionResult();  // TODO: cosa deve contenere ActionResult?
    }

    @Override
    public ActionResult buildBuilding(String playerId, String buildingId) {
        return new ActionResult();
    }

    @Override
    public ActionResult takeExtraCard(String playerId, int index) {
        return new ActionResult();
    }

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

// Metodi usati da altre classi

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

    public void setPendingExtraDrawPlayerId(String playerId) {
        this.pendingExtraDrawPlayerId = playerId;
    }

    public void clearPendingExtraDrawPlayer() {
        this.pendingExtraDrawPlayerId = null;
    }

    public void applyFoodCostWithPointsFallback(Player player, int cost) {
        // player deve pagare il costo "cost", con fallback su perdita di punti se non ha abbastanza cibo
    }

    // metodi usati dal controller per iscriversi/disiscriversi
    public void addObserver(ModelObserver o) {
        observers.add(o);
    }

    public void removeObserver(ModelObserver o) {
        observers.remove(o);
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
    private void returnPlayerToTurnOrder(Player player) {
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

    // meotdi per notifiche
    private void notifyGameStarted() {
        for (ModelObserver o : observers) o.onGameStarted();
    }

    private void notifyGameStateChanged() {
        for (ModelObserver o : observers) o.onGameStateChanged(gameState);
    }

    private void notifyGameOver() {
        for (ModelObserver o : observers) o.onGameOver();
    }

}
