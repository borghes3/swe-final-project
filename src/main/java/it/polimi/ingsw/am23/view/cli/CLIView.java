package it.polimi.ingsw.am23.view.cli;

import de.codeshelf.consoleui.prompt.ConsolePrompt;
import de.codeshelf.consoleui.prompt.InputResult;
import de.codeshelf.consoleui.prompt.ListResult;
import de.codeshelf.consoleui.prompt.builder.PromptBuilder;
import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;
import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.enums.RowType;
import it.polimi.ingsw.am23.model.payloads.*;
import it.polimi.ingsw.am23.model.state.*;
import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.NetworkSetter;
import it.polimi.ingsw.am23.network.VirtualServer;
import it.polimi.ingsw.am23.network.VirtualView;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Command line view for the Mesos client. It talks to the server through a
 * {@link VirtualServer}, keeps a local copy of the game state, reads commands
 * from the user and prints what is going on. Every server callback updates the
 * cached state and triggers a re-render of the current screen.
 */
public final class CLIView implements VirtualView {

    private static final String TITLE_MARKER = "◆";
    private static final String INFO_MARKER = "ℹ";
    private static final String SUCCESS_MARKER = "✓";
    private static final String WARNING_MARKER = "⚠";
    private static final String ERROR_MARKER = "✖";

    private final CLIPreLobbyRenderer preLobbyRenderer = new CLIPreLobbyRenderer();
    private final CLIBoardRenderer boardRenderer = new CLIBoardRenderer();
    private final CLITribeRenderer tribeRenderer = new CLITribeRenderer();

    private final LineReader lineReader;
    private volatile CountDownLatch connectedLatch = new CountDownLatch(1);

    private volatile VirtualServer server;
    private volatile String playerId;
    private volatile String playerName;
    private volatile String currentLobbyId;
    private volatile boolean owner;
    private volatile String connectError;
    private volatile List<LobbyState> lobbies = List.of();

    private volatile GameState currentGameState;
    private volatile CardState currentPeekedCard;

    /**
     * Creates a new CLI view bound to the given line reader.
     *
     * @param lineReader the JLine reader used to read user input; cannot be null
     */
    public CLIView(LineReader lineReader) {
        this.lineReader = Objects.requireNonNull(lineReader, "lineReader cannot be null");
    }

    /**
     * Entry point of the CLI client. Builds the terminal, asks the user for the
     * host, the connection type and a nickname, then connects to the server and
     * starts the main command loop. The user is asked again for the nickname
     * until the server accepts it.
     *
     * @param args ignored
     * @throws Exception if the terminal cannot be created
     */
    public static void main(String[] args) throws Exception {
        try (Terminal terminal = TerminalBuilder.builder()
                .name("Mesos CLI Client")
                .encoding(StandardCharsets.UTF_8)
                .system(true)
                .build()) {

            LineReader reader = LineReaderBuilder.builder()
                    .completer(new StringsCompleter(
                            "refresh", "create", "join", "leave",
                            "start", "place", "take", "extra", "skip", "state", "peek", "quit", "exit"
                    ))
                    .option(LineReader.Option.AUTO_LIST, true)
                    .option(LineReader.Option.LIST_PACKED, true)
                    .option(LineReader.Option.AUTO_MENU, true)
                    .option(LineReader.Option.MENU_COMPLETE, true)
                    .terminal(terminal)
                    .build();

            ConsolePrompt prompt = new ConsolePrompt();
            PromptBuilder builder;

            builder = prompt.getPromptBuilder();
            builder.createInputPrompt()
                    .name("host")
                    .message("Server host (localhost):")
                    .defaultValue("localhost")
                    .addPrompt();
            HashMap<String, ?> hostResult = prompt.prompt(builder.build());
            String host = ((InputResult) hostResult.get("host")).getInput();

            builder = prompt.getPromptBuilder();
            builder.createListPrompt()
                    .name("connection")
                    .message("Connection type")
                    .newItem("RMI").text("RMI").add()
                    .newItem("SOCKET").text("Socket").add()
                    .addPrompt();
            HashMap<String, ?> connectionResult = prompt.prompt(builder.build());
            String connection = ((ListResult) connectionResult.get("connection")).getSelectedId();

            CLIView view = new CLIView(reader);

            boolean connected = false;
            while (!connected) {
                String nick = null;
                while (nick == null || nick.isBlank()) {
                    builder = prompt.getPromptBuilder();
                    builder.createInputPrompt()
                            .name("nick")
                            .message("Nickname:")
                            .addPrompt();
                    HashMap<String, ?> nickResult = prompt.prompt(builder.build());
                    nick = ((InputResult) nickResult.get("nick")).getInput();

                    if (nick == null || nick.isBlank()) {
                        System.out.println(WARNING_MARKER + " Input a valid nickname to continue.");
                    }
                }

                try {
                    view.connect(host, nick, connection);
                    connected = true;
                } catch (IllegalStateException e) {
                    System.out.println(ERROR_MARKER + " " + e.getMessage());
                    System.out.println(WARNING_MARKER + " Choose a different nickname.");
                }
            }
            view.run();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens a connection to the server and waits until the handshake is done.
     * On success the lobby list is requested right away so the user can see
     * what is available. If the server rejects the nickname an
     * {@link IllegalStateException} is thrown with the reason, so the caller
     * can ask the user to pick another one.
     *
     * @param host           the server host name or address
     * @param nickname       the nickname requested by the user
     * @param connectionType either {@code "RMI"} or {@code "SOCKET"}
     * @throws Exception if the network setup fails or the server refuses the connection
     */
    public void connect(String host, String nickname, String connectionType) throws Exception {
        this.playerName = Objects.requireNonNull(nickname, "nickname cannot be null").trim();
        this.connectError = null;
        this.playerId = null;
        this.connectedLatch = new CountDownLatch(1);
        this.server = NetworkSetter.connect(host, playerName, this, connectionType);
        awaitConnected();
        if (playerId != null) {
            server.requestLobbyList(playerId);
        }
    }

    /**
     * Blocks until the server has answered the connection request, either by
     * accepting the nickname or by reporting an error. If the server reported
     * an error, it is re-thrown as an {@link IllegalStateException}.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    private void awaitConnected() throws InterruptedException {
        connectedLatch.await();
        if (connectError != null) {
            throw new IllegalStateException(connectError);
        }
    }

    /**
     * Main loop. Draws the current screen, then keeps reading commands from
     * the line reader until the user quits or the input stream is closed. Any
     * exception thrown by a command is caught and shown on the screen so the
     * loop never dies because of a bad input.
     */
    public void run() {
        renderCurrentScreen(null);

        while (true) {
            String line;
            try {
                line = lineReader.readLine("> ");
            } catch (UserInterruptException | EndOfFileException e) {
                return;
            }

            String trimmed = line == null ? "" : line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            try {
                if (handleCommand(trimmed)) {
                    return;
                }
            } catch (Exception e) {
                synchronized (this) {
                    renderCurrentScreen(ERROR_MARKER + " Command failed to execute: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public synchronized void onConnected(String playerId, List<LobbyState> lobbies) {
        this.playerId = playerId;
        this.lobbies = List.copyOf(lobbies);
        renderCurrentScreen(INFO_MARKER + " Connected as " + playerName + " [" + playerId + "]");
        connectedLatch.countDown();
    }

    @Override
    public synchronized void onConnectError(String reason) {
        this.connectError = reason;
        connectedLatch.countDown();
    }

    @Override
    public synchronized void onLobbyListUpdated(List<LobbyState> lobbies) {
        this.lobbies = List.copyOf(lobbies);
        renderCurrentScreen(INFO_MARKER + " Updated lobby list.");
    }

    @Override
    public synchronized void onLobbyCreated(LobbyState lobby) {
        this.currentLobbyId = lobby.getLobbyId();
        this.owner = true;
        this.lobbies = mergeLobbyIntoList(this.lobbies, lobby);
        renderCurrentScreen(SUCCESS_MARKER + " Lobby created.");
    }

    @Override
    public synchronized void onLobbyUpdate(LobbyState lobby) {
        if (currentGameState != null) {
            this.currentGameState = null;
            this.currentPeekedCard = null;
            this.currentLobbyId = null;
            this.owner = false;
            this.lobbies = mergeLobbyIntoList(this.lobbies, lobby);
            renderCurrentScreen(WARNING_MARKER + " Game interrupted: a player left. Back to lobby.");
            return;
        }
        if (containsPlayer(lobby, playerId)) {
            this.currentLobbyId = lobby.getLobbyId();
            this.owner = Objects.equals(lobby.getOwnerPlayerId(), playerId);
        }
        this.lobbies = mergeLobbyIntoList(this.lobbies, lobby);
        renderCurrentScreen(INFO_MARKER + " Lobby updated.");
    }

    @Override
    public synchronized void onJoinError(String reason) {
        renderCurrentScreen(ERROR_MARKER + " Failed to join: " + reason);
    }

    @Override
    public synchronized void onLobbyClosed() {
        this.currentLobbyId = null;
        this.owner = false;
        this.currentGameState = null;
        this.currentPeekedCard = null;

        renderCurrentScreen(WARNING_MARKER + " Lobby has been closed.");
    }

    @Override
    public synchronized void onGameStarted(GameStartedPayload payload) {
        this.currentGameState = payload.fullSnapshot();
        renderCurrentScreen(SUCCESS_MARKER + " Game started.");
    }

    @Override
    public synchronized void onTotemPlaced(TotemPlacedPayload payload) {
        if (currentGameState == null) {
            return;
        }

        BoardState board = currentGameState.board();

        List<OfferTileState> updatedTiles = board.offerTiles().stream()
                .map(tile -> tile.tileId() == payload.offerTileChar()
                        ? new OfferTileState(
                        tile.positionIndex(),
                        tile.tileId(),
                        payload.playerId(),
                        tile.minPlayers(),
                        tile.topDrawCount(),
                        tile.bottomDrawCount(),
                        tile.foodReward()
                )
                        : tile)
                .toList();

        List<TurnOrderSlotState> updatedSlots = board.turnOrderSlots().stream()
                .map(s -> Objects.equals(s.occupiedByPlayerId(), payload.playerId())
                        ? new TurnOrderSlotState(s.positionIndex(), s.foodDelta(), null)
                        : s)
                .toList();

        BoardState newBoard = rebuildBoard(
                board.topRow(),
                board.bottomRow(),
                board.topBuildings(),
                board.bottomBuildings(),
                updatedTiles,
                updatedSlots
        );

        currentGameState = new GameState(
                currentGameState.currentEra(),
                currentGameState.currentRound(),
                currentGameState.phase(),
                payload.nextPlayerId(),
                currentGameState.players(),
                newBoard,
                currentGameState.skipAllowed()
        );

        renderCurrentScreen(
                INFO_MARKER + " " + nickOf(payload.playerId())
                        + " has placed totem on ["
                        + payload.offerTileChar()
                        + "]."
        );
    }

    @Override
    public synchronized void onEndOfPlacingPhase(EndOfPlacingPhasePayload payload) {
        // All totems have been placed: clear the turn order slots so the next phase starts clean.
        if (currentGameState == null) return;
        BoardState board = currentGameState.board();
        List<TurnOrderSlotState> updatedSlots = board.turnOrderSlots().stream()
                .map(s -> new TurnOrderSlotState(s.positionIndex(), s.foodDelta(), null))
                .toList();
        currentGameState = new GameState(
                currentGameState.currentEra(), currentGameState.currentRound(),
                GamePhase.RESOLVING_OFFERS,
                payload.firstPlayerId(),
                currentGameState.players(),
                rebuildBoard(board.topRow(), board.bottomRow(),
                        board.topBuildings(), board.bottomBuildings(),
                        board.offerTiles(), updatedSlots),
                payload.skipAllowed()
        );
        renderCurrentScreen(INFO_MARKER + " Placing Phase Completed.");
    }

    @Override
    public synchronized void onCardsTaken(CardsTakenPayload payload) {
        if (currentGameState == null) return;
        BoardState board = currentGameState.board();

        List<CardState> newTopRow = removeCardsById(board.topRow(), payload.takenCardIds(), payload.takenBuildingIds());
        List<CardState> newBottomRow = removeCardsById(board.bottomRow(), payload.takenCardIds(), payload.takenBuildingIds());
        List<CardState> newTopBuildings = removeCardsById(board.topBuildings(), payload.takenCardIds(), payload.takenBuildingIds());
        List<CardState> newBottomBuildings = removeCardsById(board.bottomBuildings(), payload.takenCardIds(), payload.takenBuildingIds());

        boolean turnFinished = !(payload.newPhase() == GamePhase.RESOLVING_OFFERS
                && payload.playerId().equals(payload.nextPlayerId()));

        List<OfferTileState> clearedTiles = board.offerTiles().stream()
                .map(t -> turnFinished && Objects.equals(t.occupiedByPlayerId(), payload.playerId())
                        ? new OfferTileState(t.positionIndex(), t.tileId(), null,
                        t.minPlayers(), t.topDrawCount(), t.bottomDrawCount(), t.foodReward())
                        : t)
                .toList();

        List<TurnOrderSlotState> updatedSlots = turnFinished
                ? updateTurnOrderSlot(board.turnOrderSlots(), payload.turnOrderSlotIndex(), payload.playerId())
                : board.turnOrderSlots();

        BoardState newBoard = rebuildBoard(newTopRow, newBottomRow,
                newTopBuildings, newBottomBuildings, clearedTiles, updatedSlots);

        List<PlayerState> updatedPlayers = currentGameState.players().stream()
                .map(p -> p.playerId().equals(payload.playerId())
                        ? applyCardDeltaToPlayer(p, payload)
                        : p)
                .toList();

        currentGameState = new GameState(
                currentGameState.currentEra(), currentGameState.currentRound(),
                payload.newPhase(),
                payload.nextPlayerId(),
                updatedPlayers,
                newBoard,
                payload.skipAllowed()
        );
        renderCurrentScreen(INFO_MARKER + " " + nickOf(payload.playerId()) + " has taken the cards.");

    }

    @Override
    public synchronized void onExtraDrawRequest(ExtraDrawRequestPayload payload) {
        if (currentGameState == null) return;
        renderCurrentScreen(WARNING_MARKER + " Extra draw requested for: " + safeText(payload.pendingPlayerId()));
    }

    @Override
    public synchronized void onExtraCardTaken(ExtraCardTakenPayload payload) {
        if (currentGameState == null) {
            return;
        }

        BoardState board = currentGameState.board();
        List<String> cardId = List.of(payload.cardId());

        List<CardState> newTopRow = removeCardsById(board.topRow(), cardId, List.of());
        List<CardState> newBottomRow = removeCardsById(board.bottomRow(), cardId, List.of());
        List<CardState> newTopBuildings = removeCardsById(board.topBuildings(), List.of(), cardId);
        List<CardState> newBottomBuildings = removeCardsById(board.bottomBuildings(), List.of(), cardId);

        BoardState newBoard = rebuildBoard(
                newTopRow,
                newBottomRow,
                newTopBuildings,
                newBottomBuildings,
                board.offerTiles(),
                board.turnOrderSlots()
        );

        List<PlayerState> updatedPlayers = currentGameState.players().stream()
                .map(p -> p.playerId().equals(payload.playerId())
                        ? applyExtraCardDeltaToPlayer(p, payload)
                        : p)
                .toList();

        currentGameState = new GameState(
                currentGameState.currentEra(),
                currentGameState.currentRound(),
                payload.newPhase(),
                null,
                updatedPlayers,
                newBoard,
                payload.skipAllowed()
        );

        renderCurrentScreen(INFO_MARKER + " " + nickOf(payload.playerId()) + " has taken the extra card.");
    }

    @Override
    public synchronized void onEventResolved(EventResolvedPayload payload) {
        // Apply the food and prestige changes that the event produced for each player.
        if (currentGameState == null) return;
        List<PlayerState> updatedPlayers = applyPlayerDeltas(currentGameState.players(), payload.playerDeltas());
        currentGameState = rebuildWithPlayers(currentGameState, updatedPlayers);
        renderCurrentScreen(INFO_MARKER + " Event resolved: " + payload.eventCardId());
    }

    @Override
    public synchronized void onMarketRefreshed(MarketRefresherPayload payload) {
        if (currentGameState == null) return;
        BoardState board = currentGameState.board();

        // Drop the cards the server marked as discarded from the bottom row.
        List<CardState> newBottomRow = board.bottomRow().stream()
                .filter(c -> !payload.discardedCardIds().contains(c.getCardId()))
                .toList();

        // Move the cards that have to slide down from the top row to the bottom row.
        List<CardState> movedCards = board.topRow().stream()
                .filter(c -> payload.movedBottomCardIds().contains(c.getCardId()))
                .toList();
        List<CardState> mergedBottom = new ArrayList<>(newBottomRow);
        mergedBottom.addAll(movedCards);

        // New top row = whatever stayed on top + the brand new cards sent by the server.
        List<CardState> remainingTop = board.topRow().stream()
                .filter(c -> !payload.movedBottomCardIds().contains(c.getCardId()))
                .toList();
        List<CardState> newUpperRow = new ArrayList<>(remainingTop);
        newUpperRow.addAll(payload.newUpperRowCards());

        BoardState newBoard = rebuildBoard(newUpperRow, mergedBottom,
                board.topBuildings(), board.bottomBuildings(),
                payload.offerTiles(),
                payload.turnOrderSlots());

        currentGameState = new GameState(
                currentGameState.currentEra(),
                payload.newRound(),
                payload.newPhase(),
                payload.nextPlayerId(),
                currentGameState.players(),
                newBoard,
                payload.skipAllowed()
        );
        renderCurrentScreen(INFO_MARKER + " Market Refreshed.");
    }

    @Override
    public synchronized void onEraProgression(EraProgressionPayload payload) {
        if (currentGameState == null) return;
        BoardState board = currentGameState.board();

        // Old top buildings slide down to the bottom row, except the ones that were discarded.
        List<CardState> newBottomBuildings = board.topBuildings().stream()
                .filter(c -> !payload.discardedBuildingIds().contains(c.getCardId()))
                .toList();

        // The new era buildings replace the top row entirely.
        List<CardState> newTopBuildings = new ArrayList<>(payload.newBuildingCards());

        BoardState newBoard = rebuildBoard(board.topRow(), board.bottomRow(),
                newTopBuildings, newBottomBuildings,
                board.offerTiles(), board.turnOrderSlots());
        currentGameState = new GameState(
                payload.newEra(),
                currentGameState.currentRound(),
                currentGameState.phase(),
                currentGameState.currentPlayerId(),
                currentGameState.players(),
                newBoard,
                currentGameState.skipAllowed()
        );
        renderCurrentScreen(TITLE_MARKER + " New era: " + payload.newEra());
    }

    @Override
    public synchronized void onGameOver() {
        renderCurrentScreen(TITLE_MARKER + " Game Over.");
    }

    @Override
    public synchronized void onScoreboardAvailable(ScoreBoardPayload payload) {
        // Print the final scoreboard, players sorted from highest to lowest score.
        System.out.println();
        System.out.println(TITLE_MARKER + " FINAL SCOREBOARD " + TITLE_MARKER);
        payload.scores().stream()
                .sorted(Comparator.comparingInt(PlayerScore::totalPrestigePoints).reversed())
                .forEach(s -> System.out.println("  " + s.nickname() + ": " + s.totalPrestigePoints() + " PP"));
        System.out.println();
    }

    @Override
    public synchronized void onMatchRankingsAvailable(MatchRankingsPayload payload) {
        if (payload == null) return;
        System.out.println();
        if (!payload.persistenceAvailable()) {
            System.out.println(INFO_MARKER + " Global leaderboard unavailable (DB offline).");
            return;
        }
        Integer pos = payload.positionByPlayerId() != null
                ? payload.positionByPlayerId().get(playerId)
                : null;
        if (pos != null && pos > 0) {
            System.out.println(TITLE_MARKER + " You are #" + pos + " in the global leaderboard for "
                    + payload.playerCount() + "-player games.");
        }
        if (payload.topEntries() != null && !payload.topEntries().isEmpty()) {
            System.out.println("Top " + payload.topEntries().size() + " games for "
                    + payload.playerCount() + " players:");
            payload.topEntries().forEach(e ->
                    System.out.println("  " + e.position() + "° " + e.nickname() + " - " + e.score() + " PP"));
        }
    }

    @Override
    public synchronized void onLeaderboardAvailable(LeaderboardPayload payload) {
        if (payload == null) return;
        System.out.println();
        if (!payload.persistenceAvailable()) {
            System.out.println(INFO_MARKER + " Leaderboard unavailable (DB offline).");
            return;
        }
        System.out.println(TITLE_MARKER + " Full leaderboard - " + payload.playerCount() + "-player games");
        if (payload.entries() == null || payload.entries().isEmpty()) {
            System.out.println("  (No games recorded.)");
            return;
        }
        payload.entries().forEach(e ->
                System.out.println("  " + e.position() + "° " + e.nickname() + " - " + e.score() + " PP   " + e.matchDate()));
    }

    @Override
    public synchronized void onActionError(ActionType actionType, String message) {
        renderCurrentScreen(ERROR_MARKER + " Error " + actionType + ": " + message);
    }

    @Override
    public synchronized void onServerCrashed() {
        NetworkSetter.stopHeartbeat();
        renderCurrentScreen(ERROR_MARKER + " Connection lost.");
        System.exit(0);
    }

    /**
     * Adds to a player the characters and buildings they just took, skipping
     * any card that is already in their tribe, and updates the food to the
     * absolute value reported by the server.
     *
     * @param p       the player snapshot before the draw
     * @param payload the server payload describing what was taken
     * @return a new player snapshot with the cards and food updated
     */
    private PlayerState applyCardDeltaToPlayer(PlayerState p, CardsTakenPayload payload) {
        List<CardState> newCharacters = new ArrayList<>(p.characters());
        Set<String> existingCharacterIds = newCharacters.stream()
                .map(CardState::getCardId)
                .collect(java.util.stream.Collectors.toSet());

        for (CardState card : payload.takenCards()) {
            if (existingCharacterIds.add(card.getCardId())) {
                newCharacters.add(card);
            }
        }

        List<CardState> newBuildings = new ArrayList<>(p.buildings());
        Set<String> existingBuildingIds = newBuildings.stream()
                .map(CardState::getCardId)
                .collect(java.util.stream.Collectors.toSet());

        for (CardState building : payload.takenBuildings()) {
            if (existingBuildingIds.add(building.getCardId())) {
                newBuildings.add(building);
            }
        }

        return new PlayerState(
                p.playerId(),
                p.nickname(),
                payload.absoluteFood(),
                p.prestigePoints(),
                p.totemColor(),
                newCharacters,
                newBuildings
        );
    }

    /**
     * Adds to a player the single extra card they just drew (either a tribe
     * card or a building) and updates their food to the absolute value sent
     * by the server. If the card is already in the tribe nothing is added.
     *
     * @param p       the player snapshot before the extra draw
     * @param payload the server payload describing the extra card
     * @return a new player snapshot reflecting the extra draw
     */
    private PlayerState applyExtraCardDeltaToPlayer(PlayerState p, ExtraCardTakenPayload payload) {
        List<CardState> newCharacters = new ArrayList<>(p.characters());
        List<CardState> newBuildings = new ArrayList<>(p.buildings());

        if (payload.building()) {
            boolean alreadyPresent = newBuildings.stream()
                    .anyMatch(card -> card.getCardId().equals(payload.takenCard().getCardId()));

            if (!alreadyPresent) {
                newBuildings.add(payload.takenCard());
            }
        } else {
            boolean alreadyPresent = newCharacters.stream()
                    .anyMatch(card -> card.getCardId().equals(payload.takenCard().getCardId()));

            if (!alreadyPresent) {
                newCharacters.add(payload.takenCard());
            }
        }

        return new PlayerState(
                p.playerId(),
                p.nickname(),
                payload.absoluteFood(),
                p.prestigePoints(),
                p.totemColor(),
                newCharacters,
                newBuildings
        );
    }

    /**
     * Returns a new list of players where the food and prestige of each
     * player that appears in {@code deltas} is replaced with the absolute
     * values the server sent. Players that have no delta are kept as they
     * are.
     *
     * @param players the current player list
     * @param deltas  per-player food and prestige updates
     * @return a new list with the updated values
     */
    private List<PlayerState> applyPlayerDeltas(List<PlayerState> players, List<PlayerDelta> deltas) {
        Map<String, PlayerDelta> deltaMap = new HashMap<>();
        for (PlayerDelta d : deltas) deltaMap.put(d.playerId(), d);
        return players.stream().map(p -> {
            PlayerDelta d = deltaMap.get(p.playerId());
            if (d == null) return p;
            return new PlayerState(p.playerId(), p.nickname(),
                    d.absoluteFood(),
                    d.absolutePrestige(),
                    p.totemColor(), p.characters(), p.buildings());
        }).toList();
    }

    /**
     * Returns a copy of {@code cards} without the entries whose id appears in
     * either {@code cardIds} or {@code buildingIds}. Both lists are merged
     * because a card list can hold either kind.
     *
     * @param cards       the source list of cards
     * @param cardIds     ids of regular cards to drop
     * @param buildingIds ids of buildings to drop
     * @return a new list with the matching cards removed
     */
    private List<CardState> removeCardsById(List<CardState> cards, List<String> cardIds, List<String> buildingIds) {
        Set<String> toRemove = new HashSet<>(cardIds);
        toRemove.addAll(buildingIds);
        return cards.stream().filter(c -> !toRemove.contains(c.getCardId())).toList();
    }

    /**
     * Returns a new turn order slot list where the slot at {@code slotIndex}
     * is now occupied by {@code playerId}. The other slots are kept as they
     * are.
     *
     * @param slots     the current slot list
     * @param slotIndex the slot to update
     * @param playerId  the player that now sits on that slot
     * @return the updated slot list
     */
    private List<TurnOrderSlotState> updateTurnOrderSlot(List<TurnOrderSlotState> slots, int slotIndex, String playerId) {
        return slots.stream().map(s -> s.positionIndex() == slotIndex
                ? new TurnOrderSlotState(slotIndex, s.foodDelta(), playerId)
                : s).toList();
    }

    /**
     * Small helper that builds a {@link BoardState} from its parts. It only
     * exists to keep the call sites short.
     *
     * @param topRow          top row of cards
     * @param bottomRow       bottom row of cards
     * @param topBuildings    top row of buildings
     * @param bottomBuildings bottom row of buildings
     * @param offerTiles      offer tiles in their current state
     * @param turnOrderSlots  turn order slots in their current state
     * @return the assembled board state
     */
    private BoardState rebuildBoard(
            List<CardState> topRow, List<CardState> bottomRow,
            List<CardState> topBuildings, List<CardState> bottomBuildings,
            List<OfferTileState> offerTiles, List<TurnOrderSlotState> turnOrderSlots) {
        return new BoardState(topRow, bottomRow, topBuildings, bottomBuildings, offerTiles, turnOrderSlots);
    }

    /**
     * Builds a new {@link GameState} that is identical to {@code gs} except
     * for the player list, which is replaced with {@code newPlayers}.
     *
     * @param gs         the previous game state
     * @param newPlayers the updated player list
     * @return a new game state with the new players
     */
    private GameState rebuildWithPlayers(GameState gs, List<PlayerState> newPlayers) {
        return new GameState(gs.currentEra(), gs.currentRound(), gs.phase(),
                gs.currentPlayerId(), newPlayers, gs.board(), gs.skipAllowed());
    }

    /**
     * Returns the nickname of a player given its id, falling back to a safe
     * placeholder when the id is unknown or the game state is not loaded yet.
     *
     * @param pid the player id, may be null
     * @return the nickname if found, otherwise a safe textual fallback
     */
    private String nickOf(String pid) {
        if (pid == null || currentGameState == null) return safeText(pid);
        return currentGameState.players().stream()
                .filter(p -> Objects.equals(p.playerId(), pid))
                .map(PlayerState::nickname)
                .findFirst()
                .orElse(safeText(pid));
    }

    /**
     * Parses one line of user input and runs the matching command. Returns
     * {@code true} only when the user asked to leave (the {@code quit} or
     * {@code exit} commands), so the caller can stop the main loop.
     *
     * @param line the raw line typed by the user (already trimmed)
     * @return {@code true} if the loop must exit, {@code false} to keep going
     * @throws Exception if the command code throws
     */
    private boolean handleCommand(String line) throws Exception {
        String[] tokens = line.split("\\s+");
        String command = tokens[0].toLowerCase();

        if (!"peek".equals(command)) {
            currentPeekedCard = null;
        }

        return switch (command) {
            case "refresh" -> {
                ensureConnected();
                if (server != null && playerId != null) {
                    server.requestLobbyList(playerId);
                }
                yield false;
            }
            case "create" -> {
                ensureConnected();
                String lobbyName = line.substring(command.length()).trim();
                if (lobbyName.isEmpty()) {
                    printWarning("Use: create <lobby-name>");
                } else {
                    ConsolePrompt prompt = new ConsolePrompt();
                    PromptBuilder builder = prompt.getPromptBuilder();
                    builder.createListPrompt()
                            .name("maxPlayers")
                            .message("Max players")
                            .newItem("2").text("2").add()
                            .newItem("3").text("3").add()
                            .newItem("4").text("4").add()
                            .newItem("5").text("5").add()
                            .addPrompt();
                    HashMap<String, ?> result = prompt.prompt(builder.build());
                    int maxPlayers = Integer.parseInt(
                            ((ListResult) result.get("maxPlayers")).getSelectedId()
                    );
                    server.createLobby(playerId, lobbyName, maxPlayers);
                }
                yield false;
            }
            case "join" -> {
                ensureConnected();
                if (tokens.length < 2) {
                    printWarning("Use: join <code>");
                } else {
                    server.joinLobby(playerId, tokens[1]);
                }
                yield false;
            }
            case "leave" -> {
                ensureConnected();
                if (currentLobbyId == null) {
                    printWarning("You are not inside of a lobby.");
                } else {
                    server.leaveLobby(playerId, currentLobbyId);
                }
                yield false;
            }
            case "start" -> {
                ensureConnected();
                if (currentLobbyId == null) {
                    printWarning("You are not inside of a lobby.");
                } else if (!owner) {
                    printWarning("Only the lobby creator can start the game.");
                } else {
                    server.startGame(playerId, currentLobbyId);
                }
                yield false;
            }
            case "place" -> {
                ensureConnected();
                if (tokens.length < 2) {
                    printWarning("Use: place <tile-letter>");
                } else if (currentGameState == null) {
                    printWarning("No game state available.");
                } else {
                    char tile = Character.toUpperCase(tokens[1].charAt(0));
                    boolean exists = currentGameState.board().offerTiles().stream()
                            .anyMatch(t -> Character.toUpperCase(t.tileId()) == tile);
                    if (!exists) {
                        printWarning("No offer tile '" + tile + "' available in this game.");
                    } else {
                        server.placeTotem(playerId, tile);
                    }
                }
                yield false;
            }
            case "take" -> {
                ensureConnected();
                if (tokens.length < 4) {
                    printWarning("Use: take <top|bottom> <index> <card|building>");
                } else if (currentGameState == null) {
                    printWarning("No game state available.");

                } else {
                    String rowStr = tokens[1].toLowerCase();
                    if (!rowStr.equals("top") && !rowStr.equals("bottom")) {
                        printWarning("Row must be 'top' or 'bottom'.");
                        yield false;
                    }

                    int boardIndex;
                    try {
                        boardIndex = Integer.parseInt(tokens[2]);
                    } catch (NumberFormatException e) {
                        printWarning("Index must be a number.");
                        yield false;
                    }

                    boolean isBuilding = tokens[3].equalsIgnoreCase("building");

                    BoardState board = currentGameState.board();
                    boolean top = rowStr.equals("top");
                    int available = top
                            ? (isBuilding ? board.topBuildings().size() : board.topRow().size())
                            : (isBuilding ? board.bottomBuildings().size() : board.bottomRow().size());

                    if (boardIndex < 0 || boardIndex >= available) {
                        printWarning("Invalid index " + boardIndex + ": "
                                + (available == 0 ? "that row is empty." : "choose 0-" + (available - 1) + "."));
                        yield false;
                    }

                    SelectedSingleCard selectedSingleCard = new SelectedSingleCard(
                            rowStr.equals("top") ? RowType.TOP : RowType.BOTTOM,
                            boardIndex,
                            isBuilding
                    );
                    server.takeSingleCard(playerId, selectedSingleCard);
                }
                yield false;
            }
            case "extra" -> {
                ensureConnected();
                if (tokens.length < 2) {
                    printWarning("Use: extra <index> [card|building]");
                } else if (currentGameState == null) {
                    printWarning("No game state available.");
                } else {
                    int index;
                    try {
                        index = Integer.parseInt(tokens[1]);
                    } catch (NumberFormatException e) {
                        printWarning("Index must be a number.");
                        yield false;
                    }
                    boolean isBuilding = tokens.length >= 3 && tokens[2].equalsIgnoreCase("building");
                    BoardState board = currentGameState.board();
                    int available = isBuilding ? board.topBuildings().size() : board.topRow().size();
                    if (index < 0 || index >= available) {
                        printWarning("Invalid index " + index + ": "
                                + (available == 0 ? "nothing to draw there." : "choose 0-" + (available - 1) + "."));
                        yield false;
                    }
                    it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw selected = isBuilding
                            ? new it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw(null, index)
                            : new it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw(index, null);
                    server.takeExtraCard(playerId, selected);
                }
                yield false;
            }
            case "state" -> {
                if (currentGameState == null) {
                    renderCurrentScreen(WARNING_MARKER + " No game state available.");
                } else {
                    currentPeekedCard = null;
                    renderCurrentScreen(null);
                }
                yield false;
            }
            case "peek" -> {
                ensureConnected();
                if (currentGameState == null) {
                    printWarning("No game state available.");
                } else {
                    handlePeek(line.substring(command.length()).trim());
                }
                yield false;
            }
            case "skip" -> {
                ensureConnected();
                if (currentGameState == null) {
                    printWarning("No game state available.");
                } else {
                    server.skipTurn(playerId);
                }
                yield false;
            }
            case "quit", "exit" -> {
                if (playerId != null && server != null) {
                    try {
                        server.disconnect(playerId);
                    } catch (Exception ignored) {
                        // Best effort: if the server is already gone we just leave silently.
                    }
                }
                yield true;
            }
            default -> {
                renderCurrentScreen(WARNING_MARKER + " Unknown command.");
                yield false;
            }
        };
    }

    /**
     * Throws if the client is not connected to a server yet. Commands that
     * need to talk to the server use this to bail out with a clean error.
     *
     * @throws IllegalStateException if {@link #playerId} has not been set yet
     */
    private void ensureConnected() {
        if (playerId == null) {
            throw new IllegalStateException(connectError != null ? connectError : "Client not connected");
        }
    }

    /**
     * Checks whether a player with the given id is part of a lobby.
     *
     * @param lobby    the lobby to inspect
     * @param playerId the player id to look for
     * @return {@code true} if the player is in the lobby
     */
    private boolean containsPlayer(LobbyState lobby, String playerId) {
        return lobby.getPlayers().stream().anyMatch(player -> Objects.equals(player.id(), playerId));
    }

    /**
     * Draws the in-game screen: clears the terminal, prints the board and the
     * tribes, and, if the user was inspecting a card, prints its details too.
     *
     * @param gameState the snapshot to draw
     * @param message   an optional one-line message to show on top, may be null
     */
    private void renderGameView(GameState gameState, String message) {
        clearScreen();
        boardRenderer.render(gameState, message);
        tribeRenderer.render(gameState.players(), playerId);
        if (currentPeekedCard != null) {
            renderCardDetails(currentPeekedCard);
        }
    }

    /**
     * Draws the pre-game screen (lobby list and current lobby info).
     *
     * @param message an optional one-line message to show on top, may be null
     */
    private void renderLobbyView(String message) {
        clearScreen();
        preLobbyRenderer.render(playerName, playerId, lobbies, message, currentLobbyId);
    }

    /**
     * Draws either the game screen or the lobby screen, depending on whether
     * a game is currently running.
     *
     * @param message an optional one-line message to show on top, may be null
     */
    private void renderCurrentScreen(String message) {
        if (currentGameState != null) {
            renderGameView(currentGameState, message);
            return;
        }
        renderLobbyView(message);
    }

    /**
     * Returns a new lobby list where the lobby with the same id as
     * {@code updatedLobby} is replaced by it. If no lobby with that id is
     * found the updated lobby is appended at the end. The returned list is
     * immutable.
     *
     * @param existing     the current lobby list
     * @param updatedLobby the lobby that has just changed
     * @return the merged immutable lobby list
     */
    private List<LobbyState> mergeLobbyIntoList(List<LobbyState> existing, LobbyState updatedLobby) {
        List<LobbyState> merged = new ArrayList<>();
        boolean replaced = false;

        for (LobbyState lobby : existing) {
            if (Objects.equals(lobby.getLobbyId(), updatedLobby.getLobbyId())) {
                merged.add(updatedLobby);
                replaced = true;
            } else {
                merged.add(lobby);
            }
        }

        if (!replaced) {
            merged.add(updatedLobby);
        }

        return List.copyOf(merged);
    }

    /**
     * Handles the {@code peek} command. If the user passed a card id, looks
     * it up and shows its details. Otherwise it asks the user to pick a row
     * and then a card from an interactive menu.
     *
     * @param cardIdArgument the rest of the line after {@code peek}; may be empty
     * @throws Exception if the interactive prompt fails
     */
    private void handlePeek(String cardIdArgument) throws Exception {
        if (!cardIdArgument.isBlank()) {
            CardState selectedCard = findCardById(cardIdArgument);
            if (selectedCard == null) {
                printWarning("Card not found: " + cardIdArgument);
                return;
            }

            displayCardDetails(selectedCard);
            return;
        }

        ConsolePrompt prompt = new ConsolePrompt();
        PromptBuilder builder = prompt.getPromptBuilder();

        builder.createListPrompt()
                .name("group")
                .message("Select row")
                .newItem("TOP_CARDS").text("Top cards").add()
                .newItem("BOTTOM_CARDS").text("Bottom cards").add()
                .newItem("TOP_BUILDINGS").text("Top buildings").add()
                .newItem("BOTTOM_BUILDINGS").text("Bottom buildings").add()
                .addPrompt();

        HashMap<String, ?> groupResult = prompt.prompt(builder.build());
        String group;
        group = ((ListResult) groupResult.get("group")).getSelectedId();

        BoardState board = currentGameState.board();
        List<? extends CardState> cards;
        if (board == null) {
            printMuted("Board unavailable.");
            return;
        }

        switch (group) {
            case "TOP_CARDS" -> cards = board.topRow();
            case "BOTTOM_CARDS" -> cards = board.bottomRow();
            case "TOP_BUILDINGS" -> cards = board.topBuildings();
            case "BOTTOM_BUILDINGS" -> cards = board.bottomBuildings();
            default -> {
                printMuted("Invalid selection.");
                return;
            }
        }

        if (cards.isEmpty()) {
            printMuted("Empty");
            return;
        }

        // Ask the user which card from the chosen row they want to inspect.
        builder = prompt.getPromptBuilder();
        var listPromptBuilder = builder.createListPrompt().name("card").message("Select card");
        for (int i = 0; i < cards.size(); i++) {
            CardState card = cards.get(i);
            listPromptBuilder.newItem(Integer.toString(i)).text(card.getCardId()).add();
        }
        listPromptBuilder.addPrompt();

        HashMap<String, ?> cardResult = prompt.prompt(builder.build());
        String cardId = ((ListResult) cardResult.get("card")).getSelectedId();
        int idx = Integer.parseInt(cardId);
        CardState selected = cards.get(idx);
        displayCardDetails(selected);
    }

    /**
     * Looks up a card on the board by its id, searching both rows and both
     * building rows.
     *
     * @param cardId the id of the card to find
     * @return the matching card, or {@code null} if no card has that id
     */
    private CardState findCardById(String cardId) {
        BoardState board = currentGameState.board();
        if (board == null || cardId == null || cardId.isBlank()) {
            return null;
        }

        for (CardState card : getAllBoardCards(board)) {
            if (Objects.equals(card.getCardId(), cardId)) {
                return card;
            }
        }

        return null;
    }

    /**
     * Returns every card currently on the board (top row, bottom row, top
     * buildings and bottom buildings) in a single list.
     *
     * @param board the board snapshot
     * @return all the cards on the board, in row order
     */
    private List<CardState> getAllBoardCards(BoardState board) {
        List<CardState> cards = new ArrayList<>();
        cards.addAll(board.topRow());
        cards.addAll(board.bottomRow());
        cards.addAll(board.topBuildings());
        cards.addAll(board.bottomBuildings());
        return cards;
    }

    /**
     * Stores the card the user wants to inspect and re-renders the screen so
     * the details box shows up below the board.
     *
     * @param card the card to show
     */
    private void displayCardDetails(CardState card) {
        this.currentPeekedCard = card;
        renderCurrentScreen(INFO_MARKER + " Inspecting card " + safeText(card.getCardId()));
    }

    /**
     * Prints the card details box: a small framed area with the card id, kind,
     * era and any optional field the card has.
     *
     * @param card the card to draw
     */
    private void renderCardDetails(CardState card) {
        List<String> contentLines = buildCardDisplayLines(card);
        int width = contentLines.stream().mapToInt(String::length).max().orElse(0) + 4;

        System.out.println();
        printCardBorder(width);
        for (String line : contentLines) {
            System.out.println("| " + padRight(line, width - 4) + " |");
        }
        printCardBorder(width);
        System.out.println();
    }

    /**
     * Builds the list of text lines shown inside the card details box: the
     * header, a small ASCII art, the basic fields and the optional fields
     * that the card actually carries.
     *
     * @param card the card to describe
     * @return the lines to print, in order
     */
    private List<String> buildCardDisplayLines(CardState card) {
        List<String> lines = new ArrayList<>();
        lines.add(centerText("CARD DETAILS", 24));
        lines.add("");
        lines.addAll(centerAsciiArt());
        lines.add("");
        lines.add("Id: " + safeText(card.getCardId()));
        lines.add("Kind: " + safeText(String.valueOf(card.getCardKind())));
        lines.add("Era: " + safeText(String.valueOf(card.getEra())));
        lines.add("Prestige points: " + card.getPrintedPoints());

        addOptionalCardLine(lines, card, "Character type", "getCharacterType", "isCharacterType");
        addOptionalCardLine(lines, card, "Food symbol", "getHasFoodSymbol", "isHasFoodSymbol", "hasFoodSymbol");
        addOptionalCardLine(lines, card, "Stars", "getStars", "isStars");
        addOptionalCardLine(lines, card, "Discount", "getDiscount", "isDiscount");
        addOptionalCardLine(lines, card, "Invention icon", "getInventionIcon", "isInventionIcon");
        addOptionalCardLine(lines, card, "Food cost", "getFoodCost", "isFoodCost");
        addOptionalCardLine(lines, card, "Effect id", "getEffectId", "isEffectId");
        addOptionalCardLine(lines, card, "Min players", "getMinPlayers", "isMinPlayers");

        return lines;
    }

    /**
     * Returns the four lines of the small house ASCII art used at the top of
     * the card details box, each one centered inside the box.
     *
     * @return the centered ASCII art lines
     */
    private List<String> centerAsciiArt() {
        return List.of(
                centerText(" /\\", 36),
                centerText(" /  \\", 36),
                centerText("| [] |", 36),
                centerText("|____|", 36)
        );
    }

    /**
     * Adds a {@code "label: value"} line to {@code lines}, but only if the
     * card exposes one of the given getters and that getter returns a
     * non-null value. Used to print fields that not every card has.
     *
     * @param lines       the list to append to
     * @param card        the card to read
     * @param label       the label printed before the value
     * @param methodNames candidate getter names tried in order
     */
    private void addOptionalCardLine(List<String> lines, CardState card, String label, String... methodNames) {
        Object value = invokeGetterOptional(card, methodNames);
        if (value != null) {
            lines.add(label + ": " + value);
        }
    }

    /**
     * Prints the horizontal border of the card details box.
     *
     * @param width the total width of the border, in characters
     */
    private void printCardBorder(int width) {
        System.out.println("+" + "-".repeat(Math.max(0, width - 2)) + "+");
    }

    /**
     * Pads {@code text} with spaces on the right until it reaches {@code width}.
     * If the text is already long enough it is returned unchanged.
     *
     * @param text  the text to pad, {@code null} is treated as empty
     * @param width the target width
     * @return the padded string
     */
    private String padRight(String text, int width) {
        String value = text == null ? "" : text;
        if (value.length() >= width) {
            return value;
        }
        return value + " ".repeat(width - value.length());
    }

    /**
     * Centers {@code text} inside a field of the given width by adding spaces
     * on both sides. If the text does not fit, it is returned unchanged.
     *
     * @param text  the text to center, {@code null} is treated as empty
     * @param width the target width
     * @return the centered string
     */
    private String centerText(String text, int width) {
        String value = text == null ? "" : text;
        if (value.length() >= width) {
            return value;
        }

        int totalPadding = width - value.length();
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;
        return " ".repeat(leftPadding) + value + " ".repeat(rightPadding);
    }

    /**
     * Tries each method name in turn on {@code target} via reflection and
     * returns the first one that exists and returns a value. Used to read
     * optional fields of cards without having to know their concrete type.
     * Returns {@code null} if no getter matches or the call fails.
     *
     * @param target      the object to read from
     * @param methodNames candidate getter names tried in order
     * @return the value returned by the first matching getter, or {@code null}
     */
    private Object invokeGetterOptional(Object target, String... methodNames) {
        try {
            for (String methodName : methodNames) {
                try {
                    Method m = target.getClass().getMethod(methodName);
                    return m.invoke(target);
                } catch (NoSuchMethodException ignored) {
                    // No such getter on this card type: try the next candidate name.
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Clears the terminal using ANSI escape codes (move cursor home, clear
     * screen, clear scroll-back buffer).
     */
    private void clearScreen() {
        System.out.print("\u001B[H\u001B[2J\u001B[3J");
        System.out.flush();
    }

    /**
     * Prints a warning message with the warning marker and a {@code WARN}
     * prefix.
     *
     * @param message the warning text
     */
    private void printWarning(String message) {
        System.out.println(WARNING_MARKER + " WARN " + message);
    }

    /**
     * Prints a plain, low-emphasis message with no marker.
     *
     * @param message the text to print
     */
    private void printMuted(String message) {
        System.out.println(message);
    }

    /**
     * Returns {@code value} when it is non-null and not blank, otherwise the
     * placeholder {@code "n/a"}. Used to avoid printing empty strings.
     *
     * @param value the value to sanitize
     * @return the value or {@code "n/a"}
     */
    private String safeText(String value) {
        return value == null || value.isBlank() ? "n/a" : value;
    }

}