package it.polimi.ingsw.am23.model;

import it.polimi.ingsw.am23.exceptions.PlayerNotFoundException;
import it.polimi.ingsw.am23.model.board.*;
import it.polimi.ingsw.am23.model.cards.*;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderSlot;
import it.polimi.ingsw.am23.model.cards.turnorder.TurnOrderTile;
import it.polimi.ingsw.am23.model.deck.BuildingDeck;
import it.polimi.ingsw.am23.model.deck.TribeDeck;
import it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;
import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.enums.Era;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.enums.RowType;
import it.polimi.ingsw.am23.model.draw.CardDrawState;
import it.polimi.ingsw.am23.model.payloads.*;
import it.polimi.ingsw.am23.model.player.Player;
import it.polimi.ingsw.am23.model.resolvers.EventResolver;
import it.polimi.ingsw.am23.model.resolvers.ScoreCalculator;
import it.polimi.ingsw.am23.model.resolvers.ScoreResult;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.GameState;
import it.polimi.ingsw.am23.model.state.ScoreEntry;

import java.util.*;

/**
 * Reference implementation of {@link GameModel}.
 * Aggregates the board, the decks, the players and the current match
 * state, and drives the lifecycle through the placing, drawing, event and
 * end-of-game phases. Mutations are reported to subscribed {@link
 * ModelObserver} instances via typed payloads.
 */
public class Game implements GameModel {

    // Attributes
    private final List<Player> players;
    private final Board board;
    private final TribeDeck tribeDeck;
    private final BuildingDeck buildingDeck;
    private final EventResolver eventResolver;
    private final CardMarket cardMarket;
    private final List<ModelObserver> observers = new ArrayList<>();

    // Match state
    private GameState gameState;
    private Era currentEra;
    private int currentRound;
    private GamePhase phase;
    private String pendingExtraDrawPlayerId = null;
    private final CardDrawState drawState;
    private boolean skipAllowed = false;  // for the GUI to show the 'skip' button

    // Per-turn buffers used by CardsTakenPayload, populated during takeSingleCard
    // and cleared when the player's turn ends (setNextPhase)
    private final List<String> pendingTakenCardIds = new ArrayList<>();
    private final List<String> pendingTakenBuildingIds = new ArrayList<>();
    private int pendingFoodSpentOnBuildings = 0;
    private int pendingFoodGainedFromOfferTile = 0;
    private int pendingTurnOrderSlotIndex = -1;
    private int pendingFoodDeltaFromSlot = 0;


    private String currentPlayerId = null;

    /**
     * Builds a new game instance.
     *
     * @param players       players in the match
     * @param board         central board
     * @param tribeDeck     tribe deck used to refill the market
     * @param buildingDeck  building deck used at era progression
     * @param eventResolver resolver applied during the event phase
     * @param cardMarket    card market exposed to players
     * @param currentEra    starting era
     * @param currentRound  starting round number
     */
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
        this.drawState = new CardDrawState();
    }


    /**
     * {@inheritDoc}
     * <p>Marks the setup as completed, moves to the placing phase and
     * notifies observers.</p>
     */
    public void startGame() {
        phase = GamePhase.PLACING_TOTEMS;
        currentPlayerId = getNextPlacingPlayerId();
        gameState = buildGameState();
        notifyGameStarted();
    }

    /**
     * Looks up a player by id.
     *
     * @param playerId id of the player to retrieve
     * @return the matching player
     * @throws PlayerNotFoundException if no player matches the id
     */
    public Player findPlayer(String playerId) {
        return players.stream()
                .filter(p -> Objects.equals(p.getId(), playerId))
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException("Player " + playerId + " not found in this match"));
    }

    // ------------------------------------------
    // PLACING PHASE
    // ------------------------------------------

    /** {@inheritDoc} */
    @Override
    public ActionResult placeTotem(String playerId, char offerTileChar) {
        Player p = findPlayer(playerId);

        TurnOrderSlot currentSlot = board.getTurnOrderTile().getFirstOccupiedSlot();
        if (currentSlot == null || !Objects.equals(currentSlot.getPlayerId(), p.getId())) {
            return ActionResult.failure(
                    ActionType.PLACE_TOTEM,
                    ErrorCode.WRONG_PLAYER,
                    "It's not your turn."
            );
        }

        OfferTile tile = board.getOfferTile(offerTileChar);
        if (!tile.isFree()) {
            return ActionResult.failure(
                    ActionType.PLACE_TOTEM,
                    ErrorCode.INVALID_TILE,
                    "The selected offer tile is not empty."
            );
        }

        board.findTurnOrderSlotOccupiedBy(playerId).clear();
        tile.placeTotem(p.getId());

        currentPlayerId = getNextPlacingPlayerId();

        notifyTotemPlaced(playerId, offerTileChar, currentPlayerId);

        if (board.getTurnOrderTile().isEmpty()) {
            phase = GamePhase.RESOLVING_OFFERS;
            currentPlayerId = null;
            gameState = buildGameState();
            notifyEndOfPlacingPhase();
            processAutoResolvingOffer();
        } else {
            gameState = buildGameState();
        }

        return ActionResult.success(ActionType.PLACE_TOTEM, "Totem placed successfully");
    }

    // ------------------------------------------
    // DRAWING PHASE
    // ------------------------------------------

    /** {@inheritDoc} */
    @Override
    public ActionResult takeSingleCard(String playerId, SelectedSingleCard selectedSingleCard) {
        Player p = findPlayer(playerId);

        // Reject draws issued outside the resolving-offers phase (e.g. a late
        // click arriving after the round already moved on to events).
        if (phase != GamePhase.RESOLVING_OFFERS) {
            return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.WRONG_PHASE, "Drawing is not allowed in the current phase.");
        }

        OfferTile tile = board.getFirstOccupiedOfferTile();
        if (tile == null) {
            return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.WRONG_PHASE, "There is no offer tile to resolve.");
        }

        if (!Objects.equals(p.getId(), tile.getOccupiedByPlayerId()))
            return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.WRONG_PLAYER, "It's not your turn.");

        // Initialize the turn on the first card
        if (!drawState.isDrawingStarted()) {
            int foodReward = tile.getAction().getFoodReward();
            p.applyFoodDelta(foodReward);
            pendingFoodGainedFromOfferTile = foodReward;
            drawState.init(tile, cardMarket);
            currentPlayerId = playerId;
        }

        // Verify the player can still draw from the requested row
        if (!drawState.canDraw(selectedSingleCard)) {
            return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.INVALID_ROW, "You already picked the maximum number of cards from the " + selectedSingleCard.getRow() + " row.");
        }

        int foodDiscount = p.getTribe().getBuildingDiscount();

        // Draw the card
        if (selectedSingleCard.isBuilding()) {
            BuildingCard c = cardMarket.getBuilding(selectedSingleCard.getRow(), selectedSingleCard.getBoardIndex());
            int cost = c.getFoodCost() - foodDiscount;
            if (cost > p.getFood())
                return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.NOT_ENOUGH_FOOD, "Not enough food.");
            cardMarket.removeBuilding(selectedSingleCard.getRow(), selectedSingleCard.getBoardIndex());
            c.onTaken(this, p);
            c.getEffect().onBuildingAdded(this, p);
            c.getEffect().onAfterAllActions(this, p);
            p.applyFoodDelta(-cost);
            pendingTakenBuildingIds.add(c.getId());
            pendingFoodSpentOnBuildings += cost;

        } else {
            Card c = cardMarket.getCard(selectedSingleCard.getRow(), selectedSingleCard.getBoardIndex());
            if (!c.canBeTaken())
                return ActionResult.failure(ActionType.TAKE_CARD, ErrorCode.CARD_NOT_TAKABLE, "This card cannot be drawn.");
            cardMarket.removeCard(selectedSingleCard.getRow(), selectedSingleCard.getBoardIndex());
            c.onTaken(this, p);
            for (BuildingCard building : p.getTribe().getBuildings()) {
                building.getEffect().onCardTaken(this, p, c);
            }
            pendingTakenCardIds.add(c.getId());
        }

        drawState.incrementDrawCount(selectedSingleCard);

        if (drawState.hasFinishedDrawing()) {
            setNextPhase(playerId);
        } else {
            // Turn not finished yet: send an intermediate payload to refresh the view
            notifyCardsTakenIntermediate(playerId);
        }

        return ActionResult.success(ActionType.TAKE_CARD, "Card taken successfully.");
    }

    /** {@inheritDoc} */
    @Override
    public ActionResult skipTurn(String playerId) {
        Player p = findPlayer(playerId);

        // Extra draw skipping logic
        if (phase == GamePhase.EXTRA_DRAW) {
            if (!Objects.equals(p.getId(), pendingExtraDrawPlayerId)) {
                return ActionResult.failure(ActionType.SKIP_TURN, ErrorCode.WRONG_PLAYER, "You cannot skip, it's not your turn for the extra draw");
            }
            if (!calculateSkipAllowed()) {
                return ActionResult.failure(ActionType.SKIP_TURN, ErrorCode.CANNOT_SKIP, "You must take a character card, you cannot skip.");
            }

            // Close the extra draw and move on to events
            pendingExtraDrawPlayerId = null;
            phase = GamePhase.RESOLVING_EVENTS;
            gameState = buildGameState();
            notifyCardsTaken(playerId);
            return ActionResult.success(ActionType.SKIP_TURN, "Extra draw skipped successfully.");
        }

        // Normal draw skipping logic
        if (phase != GamePhase.RESOLVING_OFFERS) {
            return ActionResult.failure(ActionType.SKIP_TURN, ErrorCode.WRONG_PHASE, "You can only skip during the drawing phase.");
        }

        OfferTile tile = board.getFirstOccupiedOfferTile();
        if (tile == null || !Objects.equals(p.getId(), tile.getOccupiedByPlayerId())) {
            return ActionResult.failure(ActionType.SKIP_TURN, ErrorCode.WRONG_PLAYER, "It's not your turn.");
        }

        if (!calculateSkipAllowed()) {
            return ActionResult.failure(ActionType.SKIP_TURN, ErrorCode.CANNOT_SKIP, "You must take a character card, you cannot skip.");
        }

        if (!drawState.isDrawingStarted()) {
            // Only applies to offer tiles that grant a food reward
            p.applyFoodDelta(tile.getAction().getFoodReward());
        }

        setNextPhase(playerId);

        return ActionResult.success(ActionType.SKIP_TURN, "Turn skipped successfully.");
    }

    private void setNextPhase(String playerId) {
        drawState.reset();
        returnToTurnOrder(playerId);
        currentPlayerId = null;

        notifyCardsTaken(playerId);
        resetPendingDrawData();

        if (board.getFirstOccupiedOfferTile() == null) {
            if (pendingExtraDrawPlayerId != null) {
                phase = GamePhase.EXTRA_DRAW;
                gameState = buildGameState();
                notifyExtraDrawRequest();
            } else {
                phase = GamePhase.RESOLVING_EVENTS;
                gameState = buildGameState();
            }
        }
    }

    private boolean calculateSkipAllowed() {
        // Not in resolving-offer / extra-draw: not allowed
        if (!(phase == GamePhase.RESOLVING_OFFERS || phase == GamePhase.EXTRA_DRAW)) {
            return false;
        }

        String currPlayerId = computeCurrentPlayerId();
        if (currPlayerId == null) {
            return false;
        }
        boolean topRowAllowed = false;
        boolean bottomRowAllowed = false;

        if (phase == GamePhase.EXTRA_DRAW) {
            topRowAllowed = true;
        } else if (phase == GamePhase.RESOLVING_OFFERS) {
            OfferTile tile = board.getOfferTileByPlayerId(currPlayerId);
            if (tile == null) {
                return false;
            }
            // First card already drawn (multi-draw offer): check remaining draws
            if (drawState.isDrawingStarted()) {
                topRowAllowed = drawState.canDrawFromRow(RowType.TOP);
                bottomRowAllowed = drawState.canDrawFromRow(RowType.BOTTOM);
            } else {
                // First card not yet drawn
                topRowAllowed = tile.getAction().getUpperDrawRowCount() > 0;
                bottomRowAllowed = tile.getAction().getBottomDrawCount() > 0;
            }
        }

        boolean topHasCharacters = false;
        boolean bottomHasCharacters = false;

        if (topRowAllowed) {
            topHasCharacters = cardMarket.getTopRow().stream()
                    .anyMatch(card -> !(card instanceof EventCard) && card.canBeTaken());
        }

        if (bottomRowAllowed) {
            bottomHasCharacters = cardMarket.getBottomRow().stream()
                    .anyMatch(card -> !(card instanceof EventCard) && card.canBeTaken());
        }

        // Skip allowed only if no character cards are available to draw
        return !topHasCharacters && !bottomHasCharacters;
    }

    // After the player has finished drawing, move back to the turn order tile
    private void returnToTurnOrder(String playerId) {
        // Remove the player from the offer tile they were on
        OfferTile offerTile = board.getOfferTileByPlayerId(playerId);
        offerTile.clear();
        // Fetch the first free slot and place the player on it
        TurnOrderTile turnTile = board.getTurnOrderTile();
        TurnOrderSlot slot = turnTile.getFirstFreeSlot();
        slot.placeTotem(playerId);
        pendingTurnOrderSlotIndex = turnTile.getSlotIndex(slot);

        // Apply the turn order slot food delta
        if (slot.givesFood()) {                                     // positive delta
            int delta = slot.getFoodDelta();
            findPlayer(playerId).applyFoodDelta(delta);
            pendingFoodDeltaFromSlot = delta;
            for (BuildingCard building : findPlayer(playerId).getTribe().getBuildings()) {
                building.getEffect().modifyTurnOrderFood(this, findPlayer(playerId), delta);
            }
        } else if (slot.getFoodDelta() != 0) {                      // negative delta
            int delta = slot.getFoodDelta();
            if (findPlayer(playerId).canAfford(delta)) {
                findPlayer(playerId).applyFoodDelta(delta);
                pendingFoodDeltaFromSlot = delta;
            } else {
                findPlayer(playerId).spendPrestigePoints(delta * 2);
                pendingFoodDeltaFromSlot = 0; // paid in PP, not in food
            }
        }
    }

    private void processAutoResolvingOffer(){
        if (phase == GamePhase.RESOLVING_OFFERS){
            OfferTile tile = board.getFirstOccupiedOfferTile();
            if(tile != null && (tile.getAction().getUpperDrawRowCount() == 0 && tile.getAction().getBottomDrawCount() == 0)){
                String autoPlayerId = tile.getOccupiedByPlayerId();
                Player p = findPlayer(autoPlayerId);
                int foodReward = tile.getAction().getFoodReward();
                p.applyFoodDelta(foodReward);
                pendingFoodGainedFromOfferTile = foodReward;
                returnToTurnOrder(autoPlayerId);
                notifyCardsTaken(autoPlayerId);
                resetPendingDrawData();
            }
        }
    }


    // ------------------------------------------
    // RESOLVING EVENTS PHASE
    // ------------------------------------------

    /** {@inheritDoc} */
    @Override
    public ActionResult resolveEvents() {
        List<EventCard> events = cardMarket.getBottomRowEvents();

        if (currentRound == 10) {
            events = new ArrayList<>(events);
            events.addAll(cardMarket.getTopRowEvents());

            for (EventCard event : eventResolver.orderEvents(events)) {
                resolveAndNotifySingleEvent(event);
            }

            phase = GamePhase.ENDED;
            currentPlayerId = null;
            cleanUp();
            gameState = buildGameState();
            notifyGameOver();

        } else {
            for (EventCard event : eventResolver.orderEvents(events)) {
                resolveAndNotifySingleEvent(event);
            }

            currentRound++;
            phase = GamePhase.PLACING_TOTEMS;
            currentPlayerId = getNextPlacingPlayerId();

            cleanUp();
            gameState = buildGameState();
        }

        return ActionResult.success(ActionType.END_ROUND, "Events resolved successfully");
    }

    private void resolveAndNotifySingleEvent(EventCard event) {
        // Snapshot food and PP before resolution to compute the delta
        Map<String, int[]> before = snapshotPlayerStats();

        eventResolver.resolveSingleEvent(event, this);

        // Compute deltas
        List<PlayerDelta> deltas = computePlayerDeltas(before);

        notifyEventResolved(event.getId(), deltas);
    }

    // Snapshot: playerId -> [food, PP]
    private Map<String, int[]> snapshotPlayerStats() {
        Map<String, int[]> snapshot = new HashMap<>();
        for (Player p : players) {
            snapshot.put(p.getId(), new int[]{p.getFood(), p.getPrestigePoints()});
        }
        return snapshot;
    }

    private List<PlayerDelta> computePlayerDeltas(Map<String, int[]> before) {
        List<PlayerDelta> deltas = new ArrayList<>();
        for (Player p : players) {
            int[] pre = before.get(p.getId());
            int foodDelta = p.getFood() - pre[0];
            int prestigeDelta = p.getPrestigePoints() - pre[1];
            deltas.add(new PlayerDelta(p.getId(), foodDelta, prestigeDelta, p.getFood(), p.getPrestigePoints()));
        }
        return deltas;
    }


    // ------------------------------------------
    // CLEANUP PHASE (END OF ROUND) + ERA PROGRESSION
    // Prepares the board for the next round and handles era progression
    // ------------------------------------------
    private void cleanUp() {
        List<String> discardedCardIds = cardMarket.clearBottomRow();
        List<String> movedCardIds = cardMarket.moveTopRowToBottom();
        RefillResult refill = cardMarket.refillTopRow(tribeDeck, players.size(), currentEra);
        List<String> newUpperRowIds = refill.getAddedCards().stream().map(Card::getId).toList();
        List<CardState> newUpperRowCards = refill.getAddedCards().stream().map(Card::toState).toList();

        notifyMarketRefreshed(discardedCardIds, movedCardIds, newUpperRowIds, newUpperRowCards);

        if (refill.isEraAdvanced()) {
            Era newEra = refill.getNewEra();
            EraProgressionResult eraResult = cardMarket.handleEraProgression(buildingDeck, newEra);
            currentEra = newEra;
            gameState = buildGameState();
            notifyEraProgression(newEra, eraResult);
        }
    }

    // ------------------------------------------
    // ENDGAME PHASE: compute scores
    // ------------------------------------------

    /** {@inheritDoc} */
    @Override
    public ActionResult calculateScores() {
        ScoreCalculator scoreCalculator = new ScoreCalculator(this);
        List<ScoreResult> scoreBoard = scoreCalculator.calculateFinalScores();
        gameState = buildGameStateWithScores(scoreBoard);
        notifyScores(scoreBoard);
        return ActionResult.success(ActionType.GENERIC, "Scores calculated successfully");
    }

    // ------------------------------------------
    // EXTRA DRAW: at the end of the DRAWING PHASE,
    // the player saved in pendingExtraDrawPlayerId can draw an extra card.
    // Invoked by the controller when the owning player holds the extra-draw building effect.
    // ------------------------------------------

    /** {@inheritDoc} */
    @Override
    public ActionResult takeExtraCard(String playerId, SelectedCardExtraDraw selectedCardExtraDraw) {
        Player p = findPlayer(playerId);

        if (pendingExtraDrawPlayerId == null) {
            return ActionResult.failure(
                    ActionType.TAKE_CARD,
                    ErrorCode.NO_PENDING_EXTRA_DRAW,
                    "There are no pending extra draws."
            );
        }

        if (!Objects.equals(p.getId(), pendingExtraDrawPlayerId)) {
            return ActionResult.failure(
                    ActionType.TAKE_CARD,
                    ErrorCode.INVALID_EXTRA_DRAW,
                    "It's not your turn."
            );
        }

        int foodDiscount = p.getTribe().getBuildingDiscount();

        String takenCardId;
        CardState takenCardState;
        boolean takenBuilding;

        if (selectedCardExtraDraw.isTribeCard()) {
            int boardIndex = selectedCardExtraDraw.getCardIndex();
            Card c = cardMarket.getCard(RowType.TOP, boardIndex);

            if (!c.canBeTaken()) {
                return ActionResult.failure(
                        ActionType.TAKE_CARD,
                        ErrorCode.INVALID_EXTRA_DRAW,
                        "This card cannot be drawn from the card market."
                );
            }

            cardMarket.removeCard(RowType.TOP, boardIndex);
            c.onTaken(this, p);

            for (BuildingCard building : p.getTribe().getBuildings()) {
                building.getEffect().onCardTaken(this, p, c);
            }

            takenCardId = c.getId();
            takenCardState = c.toState();
            takenBuilding = false;

        } else {
            int boardIndex = selectedCardExtraDraw.getBuildingIndex();
            BuildingCard c = cardMarket.getBuilding(RowType.TOP, boardIndex);

            int effectiveCost = c.getFoodCost() - foodDiscount;
            if (effectiveCost > p.getFood()) {
                return ActionResult.failure(
                        ActionType.TAKE_CARD,
                        ErrorCode.NOT_ENOUGH_FOOD,
                        "The food cost exceeds the player's reserve."
                );
            }

            cardMarket.removeBuilding(RowType.TOP, boardIndex);
            c.onTaken(this, p);
            c.getEffect().onBuildingAdded(this, p);
            c.getEffect().onAfterAllActions(this, p);
            p.applyFoodDelta(-effectiveCost);

            takenCardId = c.getId();
            takenCardState = c.toState();
            takenBuilding = true;
        }

        pendingExtraDrawPlayerId = null;
        phase = GamePhase.RESOLVING_EVENTS;
        gameState = buildGameState();

        notifyExtraCardTaken(
                playerId,
                takenCardId,
                takenCardState,
                takenBuilding,
                p.getFood()
        );

        return ActionResult.success(ActionType.TAKE_CARD, "Extra card taken successfully");
    }

    /**
     * Schedules an extra draw for the supplied player. Invoked by the
     * extra draw building effect when the building is bought.
     *
     * @param playerId id of the player entitled to the extra draw
     */
    public void setPendingExtraDrawPlayerId(String playerId) {
        this.pendingExtraDrawPlayerId = playerId;
    }

    private void resetPendingDrawData() {
        pendingTakenCardIds.clear();
        pendingTakenBuildingIds.clear();
        pendingFoodSpentOnBuildings = 0;
        pendingFoodGainedFromOfferTile = 0;
        pendingTurnOrderSlotIndex = -1;
        pendingFoodDeltaFromSlot = 0;
    }

    // ------------------------------------------
    // GAMESTATE
    // ------------------------------------------
    private GameState buildGameState() {
        skipAllowed = calculateSkipAllowed();
        return new GameState(
                currentEra,
                currentRound,
                phase,
                computeCurrentPlayerId(),
                players.stream().map(Player::getState).toList(),
                board.getState(cardMarket),
                skipAllowed
        );
    }

    private GameState buildGameStateWithScores(List<ScoreResult> scoreBoard) {
        List<ScoreEntry> scores = scoreBoard.stream()
                .map(r -> new ScoreEntry(r.player.getId(), r.foodPoints, r.PP))
                .toList();
        skipAllowed = calculateSkipAllowed();
        return new GameState(
                currentEra,
                currentRound,
                phase,
                computeCurrentPlayerId(),
                players.stream().map(Player::getState).toList(),
                board.getState(cardMarket),
                scores,
                skipAllowed
        );
    }

    private String computeCurrentPlayerId() {
        if (phase == GamePhase.EXTRA_DRAW) {
            return pendingExtraDrawPlayerId;
        }
        return currentPlayerId;
    }
    private String getNextPlacingPlayerId() {
        TurnOrderSlot nextSlot = board.getTurnOrderTile().getFirstOccupiedSlot();
        return nextSlot != null ? nextSlot.getPlayerId() : null;
    }


    // ------------------------------------------
    // OBSERVERS AND NOTIFICATIONS
    // ------------------------------------------

    /** {@inheritDoc} */
    @Override
    public void addObserver(ModelObserver o) {
        observers.add(o);
    }

    /** {@inheritDoc} */
    @Override
    public void removeObserver(ModelObserver o) {
        observers.remove(o);
    }


    private void notifyGameStarted() {
        GameStartedPayload payload = new GameStartedPayload(buildGameState());
        observers.forEach(o -> o.onGameStarted(payload));
    }

    private void notifyTotemPlaced(String playerId, char offerTileChar, String nextPlayerId) {
        TotemPlacedPayload payload = new TotemPlacedPayload(
                playerId,
                offerTileChar,
                nextPlayerId
        );

        observers.forEach(o -> o.onTotemPlaced(payload));
    }

    private void notifyEndOfPlacingPhase() {
        // Player order on the offer track equals the occupied offer tiles in order
        List<String> playerOrder = board.getOfferTiles().stream()
                .filter(t -> !t.isFree())
                .map(OfferTile::getOccupiedByPlayerId)
                .toList();
        // The first player to draw is the first one on the offer track
        String firstPlayerId = playerOrder.isEmpty() ? null : playerOrder.get(0);
        EndOfPlacingPhasePayload payload = new EndOfPlacingPhasePayload(playerOrder, firstPlayerId, calculateSkipAllowed());
        observers.forEach(o -> o.onEndOfPlacingPhase(payload));
    }

    private void notifyCardsTaken(String playerId) {
        // Compute the phase and the next player after this turn
        GamePhase newPhase;
        String nextPlayerId;

        if (pendingExtraDrawPlayerId != null) {
            newPhase = GamePhase.EXTRA_DRAW;
            nextPlayerId = pendingExtraDrawPlayerId;
        } else if (board.getFirstOccupiedOfferTile() != null) {
            newPhase = GamePhase.RESOLVING_OFFERS;
            nextPlayerId = board.getFirstOccupiedOfferTile().getOccupiedByPlayerId();
        } else {
            newPhase = GamePhase.RESOLVING_EVENTS;
            nextPlayerId = null;
        }

        Player p = findPlayer(playerId);
        List<CardState> takenCards = p.getTribe().getCharacters().stream()
                .filter(c -> pendingTakenCardIds.contains(c.getId()))
                .map(CharacterCard::toState)
                .toList();
        List<CardState> takenBuildings = p.getTribe().getBuildings().stream()
                .filter(b -> pendingTakenBuildingIds.contains(b.getId()))
                .map(BuildingCard::toState)
                .toList();

        CardsTakenPayload payload = new CardsTakenPayload(
                playerId,
                List.copyOf(pendingTakenCardIds),
                List.copyOf(pendingTakenBuildingIds),
                pendingFoodSpentOnBuildings,
                pendingFoodGainedFromOfferTile,
                pendingTurnOrderSlotIndex,
                pendingFoodDeltaFromSlot,
                newPhase,
                nextPlayerId,
                takenCards,
                takenBuildings,
                p.getFood(),
                calculateSkipAllowed()
        );
        observers.forEach(o -> o.onCardsTaken(payload));
    }

    private void notifyCardsTakenIntermediate(String playerId) {
        Player p = findPlayer(playerId);
        List<CardState> takenCards = p.getTribe().getCharacters().stream()
                .filter(c -> pendingTakenCardIds.contains(c.getId()))
                .map(CharacterCard::toState)
                .toList();
        List<CardState> takenBuildings = p.getTribe().getBuildings().stream()
                .filter(b -> pendingTakenBuildingIds.contains(b.getId()))
                .map(BuildingCard::toState)
                .toList();

        CardsTakenPayload payload = new CardsTakenPayload(
                playerId,
                List.copyOf(pendingTakenCardIds),
                List.copyOf(pendingTakenBuildingIds),
                pendingFoodSpentOnBuildings,
                pendingFoodGainedFromOfferTile,
                -1, // slot index not known yet
                0,  // slot food delta not known yet
                GamePhase.RESOLVING_OFFERS,
                playerId, // same player: turn not finished
                takenCards,
                takenBuildings,
                p.getFood(),
                calculateSkipAllowed()
        );
        observers.forEach(o -> o.onCardsTaken(payload));
    }

    private void notifyExtraDrawRequest() {
        ExtraDrawRequestPayload payload = new ExtraDrawRequestPayload(pendingExtraDrawPlayerId);
        observers.forEach(o -> o.onExtraDrawRequest(payload));
    }

    private void notifyExtraCardTaken(String playerId,
                                      String cardId,
                                      CardState takenCard,
                                      boolean building,
                                      int absoluteFood) {
        ExtraCardTakenPayload payload = new ExtraCardTakenPayload(
                playerId,
                cardId,
                takenCard,
                building,
                absoluteFood,
                phase,
                skipAllowed
        );

        observers.forEach(o -> o.onExtraCardTaken(payload));
    }

    private void notifyEventResolved(String eventCardId, List<PlayerDelta> deltas) {
        EventResolvedPayload payload = new EventResolvedPayload(eventCardId, currentEra, deltas);
        observers.forEach(o -> o.onEventResolved(payload));
    }

    private void notifyMarketRefreshed(List<String> discardedIds,
                                       List<String> movedIds,
                                       List<String> newUpperRowIds,
                                       List<CardState> newUpperRowCards) {
        MarketRefresherPayload payload = new MarketRefresherPayload(
                discardedIds,
                movedIds,
                newUpperRowIds,
                newUpperRowCards,
                board.buildOfferTileState(),
                board.buildTurnOrderSlotsState(),
                currentRound,
                phase,
                computeCurrentPlayerId(),
                calculateSkipAllowed()
        );

        observers.forEach(o -> o.onMarketRefreshed(payload));
    }

    private void notifyEraProgression(Era newEra, EraProgressionResult result) {
        List<String> newBuildingIds = result.newTopBuildings().stream().map(BuildingCard::getId).toList();
        List<String> discardedIds = result.discardedBuildings().stream().map(BuildingCard::getId).toList();
        List<CardState> newBuildingCards = result.newTopBuildings().stream().map(BuildingCard::toState).toList();
        EraProgressionPayload payload = new EraProgressionPayload(newEra, newBuildingIds, discardedIds, newBuildingCards);
        observers.forEach(o -> o.onEraProgression(payload));
    }

    private void notifyGameOver() {
        observers.forEach(o -> o.onGameOver());
    }

    private void notifyScores(List<ScoreResult> scoreBoard) {
        List<PlayerScore> scores = scoreBoard.stream()
                .map(r -> new PlayerScore(
                        r.player.getId(),
                        r.player.getState().getNickname(),
                        r.PP,
                        r.foodPoints,
                        Map.of("food", r.foodPoints) // minimal breakdown, can be extended
                ))
                .toList();
        ScoreBoardPayload payload = new ScoreBoardPayload(scores);
        observers.forEach(o -> o.onScoreboardAvailable(payload));
    }


    // GETTERS

    /** @return the players in the match in their declaration order */
    public List<Player> getPlayers() {
        return players;
    }

    /** @return the central board */
    public Board getBoard() {
        return board;
    }

    /** {@inheritDoc} */
    @Override
    public GameState getGameState() {
        return gameState;
    }

    /** {@inheritDoc} */
    @Override
    public GamePhase getGamePhase() {
        return phase;
    }

    /** @return the era currently being played */
    public Era getCurrentEra() {
        return currentEra;
    }

}
