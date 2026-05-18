package it.polimi.ingsw.am23.view.cli;

import de.codeshelf.consoleui.prompt.ConsolePrompt;
import de.codeshelf.consoleui.prompt.InputResult;
import de.codeshelf.consoleui.prompt.ListResult;
import de.codeshelf.consoleui.prompt.builder.PromptBuilder;
import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.enums.RowType;
import it.polimi.ingsw.am23.model.payloads.*;
import it.polimi.ingsw.am23.model.state.*;
import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.NetworkSetter;
import it.polimi.ingsw.am23.network.VirtualServer;
import it.polimi.ingsw.am23.network.VirtualView;
import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

public final class CLIView implements VirtualView {

    private static final int DEFAULT_LOBBY_MAX_PLAYERS = 5;

    private static final String TITLE_MARKER = "◆";
    private static final String INFO_MARKER = "ℹ";
    private static final String SUCCESS_MARKER = "✓";
    private static final String WARNING_MARKER = "⚠";
    private static final String ERROR_MARKER = "✖";

    private final CLIPreLobbyRenderer preLobbyRenderer = new CLIPreLobbyRenderer();
    private final CLIBoardRenderer boardRenderer = new CLIBoardRenderer();

    private final LineReader lineReader;
    private final CountDownLatch connectedLatch = new CountDownLatch(1);
    private final CountDownLatch gameStartedLatch = new CountDownLatch(1);

    private volatile VirtualServer server;
    private volatile String playerId;
    private volatile String playerName;
    private volatile String currentLobbyId;
    private volatile boolean owner;
    private volatile String connectError;
    private volatile List<LobbyState> lobbies = List.of();

    private volatile GameState currentGameState;
    private volatile CardState currentPeekedCard;

    public CLIView(LineReader lineReader) {
        this.lineReader = Objects.requireNonNull(lineReader, "lineReader cannot be null");
    }

    public static void main(String[] args) throws Exception {
        try (Terminal terminal = TerminalBuilder.builder()
                .name("Mesos CLI Client")
                .encoding(StandardCharsets.UTF_8)
                .system(true)
                .build()) {

            LineReader reader = LineReaderBuilder.builder()
                    .completer(new StringsCompleter(
                                "help", "refresh", "lobbies", "create", "join", "leave",
                                "start", "place", "take", "extra", "state", "peek", "quit", "exit"
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
            view.connect(host, nick, connection);
            view.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect(String host, String nickname, String connectionType) throws Exception {
        this.playerName = Objects.requireNonNull(nickname, "nickname cannot be null").trim();
        this.server = NetworkSetter.connect(host, playerName, this, connectionType);
        awaitConnected();
        if (playerId != null) {
            server.requestLobbyList(playerId);
        }
    }

    private void awaitConnected() throws InterruptedException {
        connectedLatch.await();
        if (connectError != null) {
            throw new IllegalStateException(connectError);
        }
    }

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
                renderCurrentScreen(null);
            } catch (Exception e) {
                renderCurrentScreen(ERROR_MARKER + " Command failed to execute: " + e.getMessage());
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
        renderCurrentScreen(ERROR_MARKER + " Connection failed: " + reason);
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
        renderCurrentScreen(WARNING_MARKER + " Lobby has been closed.");
    }

    @Override
    public synchronized void onGameStarted(GameStartedPayload payload) {
        // snapshot completo iniziale
        this.currentGameState = payload.fullSnapshot();
        gameStartedLatch.countDown();
        renderCurrentScreen(SUCCESS_MARKER + " Game started.");
    }

    @Override
    public synchronized void onTotemPlaced(TotemPlacedPayload payload) {
        if (currentGameState == null) {
            return;
        }

        BoardState board = currentGameState.getBoard();

        List<OfferTileState> updatedTiles = board.getOfferTiles().stream()
                .map(tile -> tile.getTileId() == payload.offerTileChar()
                        ? new OfferTileState(
                        tile.getPositionIndex(),
                        tile.getTileId(),
                        payload.playerId(),
                        tile.getMinPlayers(),
                        tile.getTopDrawCount(),
                        tile.getBottomDrawCount(),
                        tile.getFoodReward()
                )
                        : tile)
                .toList();

        List<TurnOrderSlotState> updatedSlots = board.getTurnOrderSlots().stream()
                .map(s -> Objects.equals(s.getOccupiedByPlayerId(), payload.playerId())
                        ? new TurnOrderSlotState(s.getPositionIndex(), s.getFoodDelta(), null)
                        : s)
                .toList();

        BoardState newBoard = rebuildBoard(
                board,
                board.getTopRow(),
                board.getBottomRow(),
                board.getTopBuildings(),
                board.getBottomBuildings(),
                updatedTiles,
                updatedSlots
        );

        currentGameState = new GameState(
                currentGameState.getCurrentEra(),
                currentGameState.getCurrentRound(),
                currentGameState.getPhase(),
                payload.nextPlayerId(),
                currentGameState.getPlayers(),
                newBoard,
                currentGameState.getSkipAllowed()
        );

        renderCurrentScreen(
                INFO_MARKER + " " + payload.playerId()
                        + " ha piazzato il totem su ["
                        + payload.offerTileChar()
                        + "]."
        );
    }

        @Override
    public synchronized void onEndOfPlacingPhase(EndOfPlacingPhasePayload payload) {
        // Tutti i totem sono stati piazzati. Aggiorna l'ordine di turno.
        if (currentGameState == null) return;
        BoardState board = currentGameState.getBoard();
        List<TurnOrderSlotState> updatedSlots = buildTurnOrderSlots(payload.playerOrderOnOfferTrack());
        currentGameState = new GameState(
                currentGameState.getCurrentEra(), currentGameState.getCurrentRound(),
                GamePhase.RESOLVING_OFFERS,
                payload.firstPlayerId(),
                currentGameState.getPlayers(),
                rebuildBoard(board, board.getTopRow(), board.getBottomRow(),
                        board.getTopBuildings(), board.getBottomBuildings(),
                        board.getOfferTiles(), updatedSlots),
                payload.skipAllowed()
        );
        renderCurrentScreen(INFO_MARKER + " Fine fase di piazzamento totem.");
    }

    @Override
    public synchronized void onCardsTaken(CardsTakenPayload payload) {
        if (currentGameState == null) return;
        BoardState board = currentGameState.getBoard();

        List<CardState> newTopRow = removeCardsById(board.getTopRow(), payload.takenCardIds(), payload.takenBuildingIds());
        List<CardState> newBottomRow = removeCardsById(board.getBottomRow(), payload.takenCardIds(), payload.takenBuildingIds());
        List<CardState> newTopBuildings = removeCardsById(board.getTopBuildings(), payload.takenCardIds(), payload.takenBuildingIds());
        List<CardState> newBottomBuildings = removeCardsById(board.getBottomBuildings(), payload.takenCardIds(), payload.takenBuildingIds());

        boolean turnFinished = !(payload.newPhase() == GamePhase.RESOLVING_OFFERS
                && payload.playerId().equals(payload.nextPlayerId()));

        List<OfferTileState> clearedTiles = board.getOfferTiles().stream()
                .map(t -> turnFinished && Objects.equals(t.getOccupiedByPlayerId(), payload.playerId())
                        ? new OfferTileState(t.getPositionIndex(), t.getTileId(), null,
                        t.getMinPlayers(), t.getTopDrawCount(), t.getBottomDrawCount(), t.getFoodReward())
                        : t)
                .toList();

        List<TurnOrderSlotState> updatedSlots = turnFinished
                ? updateTurnOrderSlot(board.getTurnOrderSlots(), payload.turnOrderSlotIndex(), payload.playerId())
                : board.getTurnOrderSlots();

        BoardState newBoard = rebuildBoard(board, newTopRow, newBottomRow,
                newTopBuildings, newBottomBuildings, clearedTiles, updatedSlots);

        List<PlayerState> updatedPlayers = currentGameState.getPlayers().stream()
                .map(p -> p.getPlayerId().equals(payload.playerId())
                        ? applyCardDeltaToPlayer(p, payload)
                        : p)
                .toList();

        currentGameState = new GameState(
                currentGameState.getCurrentEra(), currentGameState.getCurrentRound(),
                payload.newPhase(),
                payload.nextPlayerId(),
                updatedPlayers,
                newBoard,
                payload.skipAllowed()
        );
        renderCurrentScreen(INFO_MARKER + " " + payload.playerId() + " ha preso le carte.");

    }

    @Override
    public synchronized void onExtraDrawRequest(ExtraDrawRequestPayload payload) {
        if (currentGameState == null) return;
        renderCurrentScreen(WARNING_MARKER + " Extra draw richiesto per: " + safeText(payload.pendingPlayerId()));
    }

    @Override
    public synchronized void onExtraCardTaken(ExtraCardTakenPayload payload) {
        if (currentGameState == null) {
            return;
        }

        BoardState board = currentGameState.getBoard();
        List<String> cardId = List.of(payload.cardId());

        List<CardState> newTopRow = removeCardsById(board.getTopRow(), cardId, List.of());
        List<CardState> newBottomRow = removeCardsById(board.getBottomRow(), cardId, List.of());
        List<CardState> newTopBuildings = removeCardsById(board.getTopBuildings(), List.of(), cardId);
        List<CardState> newBottomBuildings = removeCardsById(board.getBottomBuildings(), List.of(), cardId);

        BoardState newBoard = rebuildBoard(
                board,
                newTopRow,
                newBottomRow,
                newTopBuildings,
                newBottomBuildings,
                board.getOfferTiles(),
                board.getTurnOrderSlots()
        );

        List<PlayerState> updatedPlayers = currentGameState.getPlayers().stream()
                .map(p -> p.getPlayerId().equals(payload.playerId())
                        ? applyExtraCardDeltaToPlayer(p, payload)
                        : p)
                .toList();

        currentGameState = new GameState(
                currentGameState.getCurrentEra(),
                currentGameState.getCurrentRound(),
                payload.newPhase(),
                null,
                updatedPlayers,
                newBoard,
                payload.skipAllowed()
        );

        renderCurrentScreen(INFO_MARKER + " " + payload.playerId() + " ha preso la carta extra.");
    }

    @Override
    public synchronized void onEventResolved(EventResolvedPayload payload) {
        // Aggiorna food e PP di ogni giocatore secondo i delta.
        if (currentGameState == null) return;
        List<PlayerState> updatedPlayers = applyPlayerDeltas(currentGameState.getPlayers(), payload.playerDeltas());
        currentGameState = rebuildWithPlayers(currentGameState, updatedPlayers);
        renderCurrentScreen(INFO_MARKER + " Evento risolto: " + payload.eventCardId());
    }

    @Override
    public synchronized void onMarketRefreshed(MarketRefresherPayload payload) {
        if (currentGameState == null) return;
        BoardState board = currentGameState.getBoard();

        // scarta dalla fila inferiore
        List<CardState> newBottomRow = board.getBottomRow().stream()
                .filter(c -> !payload.discardedCardIds().contains(c.getCardId()))
                .toList();

        // sposta dalla fila superiore alla inferiore
        List<CardState> movedCards = board.getTopRow().stream()
                .filter(c -> payload.movedBottomCardIds().contains(c.getCardId()))
                .toList();
        List<CardState> mergedBottom = new ArrayList<>(newBottomRow);
        mergedBottom.addAll(movedCards);

        // nuova fila superiore = rimaste + nuove carte complete dal payload
        List<CardState> remainingTop = board.getTopRow().stream()
                .filter(c -> !payload.movedBottomCardIds().contains(c.getCardId()))
                .toList();
        List<CardState> newUpperRow = new ArrayList<>(remainingTop);
        newUpperRow.addAll(payload.newUpperRowCards()); // aggiunge le nuove carte

        BoardState newBoard = rebuildBoard(board, newUpperRow, mergedBottom,
                board.getTopBuildings(), board.getBottomBuildings(),
                payload.offerTiles(),
                payload.turnOrderSlots());

        currentGameState = new GameState(
                currentGameState.getCurrentEra(),
                payload.newRound(),
                payload.newPhase(),
                payload.nextPlayerId(),
                currentGameState.getPlayers(),
                newBoard,
                payload.skipAllowed()
        );
        renderCurrentScreen(INFO_MARKER + " Mercato aggiornato.");
    }

    @Override
    public synchronized void onEraProgression(EraProgressionPayload payload) {
        if (currentGameState == null) return;
        BoardState board = currentGameState.getBoard();

        // i vecchi topBuildings scendono in bottom (eccetto quelli scartati)
        List<CardState> newBottomBuildings = board.getTopBuildings().stream()
                .filter(c -> !payload.discardedBuildingIds().contains(c.getCardId()))
                .toList();

        // i nuovi edifici dell'era sostituiscono completamente la top
        List<CardState> newTopBuildings = new ArrayList<>(payload.newBuildingCards());

        BoardState newBoard = rebuildBoard(board, board.getTopRow(), board.getBottomRow(),
                newTopBuildings, newBottomBuildings,
                board.getOfferTiles(), board.getTurnOrderSlots());
        currentGameState = new GameState(
                payload.newEra(),
                currentGameState.getCurrentRound(),
                currentGameState.getPhase(),
                currentGameState.getCurrentPlayerId(),
                currentGameState.getPlayers(),
                newBoard,
                currentGameState.getSkipAllowed()
        );
        renderCurrentScreen(TITLE_MARKER + " Nuova era: " + payload.newEra());
    }

    @Override
    public synchronized void onGameOver() {
        renderCurrentScreen(TITLE_MARKER + " Partita terminata.");
    }

    @Override
    public synchronized void onScoreboardAvailable(ScoreBoardPayload payload) {
        // mostra il punteggio finale
        System.out.println();
        System.out.println(TITLE_MARKER + " CLASSIFICA FINALE " + TITLE_MARKER);
        payload.scores().stream()
                .sorted(Comparator.comparingInt(PlayerScore::totalPrestigePoints).reversed())
                .forEach(s -> System.out.println("  " + s.playerId() + ": " + s.totalPrestigePoints() + " PP"));
        System.out.println();
    }

    @Override
    public synchronized void onActionError(ActionType actionType, String message) {
        renderCurrentScreen(ERROR_MARKER + " Error " + actionType + ": " + message);
    }

    @Override
    public synchronized void onServerCrashed() {
        NetworkSetter.stopHeartbeat();
        renderCurrentScreen(ERROR_MARKER + " Connessione persa.");
        System.exit(0);
    }

    // applicazione delta
    private PlayerState applyCardDeltaToPlayer(PlayerState p, CardsTakenPayload payload) {
        List<CardState> newCharacters = new ArrayList<>(p.getCharacters());
        Set<String> existingCharacterIds = newCharacters.stream()
                .map(CardState::getCardId)
                .collect(java.util.stream.Collectors.toSet());

        for (CardState card : payload.takenCards()) {
            if (existingCharacterIds.add(card.getCardId())) {
                newCharacters.add(card);
            }
        }

        List<CardState> newBuildings = new ArrayList<>(p.getBuildings());
        Set<String> existingBuildingIds = newBuildings.stream()
                .map(CardState::getCardId)
                .collect(java.util.stream.Collectors.toSet());

        for (CardState building : payload.takenBuildings()) {
            if (existingBuildingIds.add(building.getCardId())) {
                newBuildings.add(building);
            }
        }

        return new PlayerState(
                p.getPlayerId(),
                p.getNickname(),
                payload.absoluteFood(),
                p.getPrestigePoints(),
                p.getTotemColor(),
                newCharacters,
                newBuildings
        );
    }
    private PlayerState applyExtraCardDeltaToPlayer(PlayerState p, ExtraCardTakenPayload payload) {
        List<CardState> newCharacters = new ArrayList<>(p.getCharacters());
        List<CardState> newBuildings = new ArrayList<>(p.getBuildings());

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
                p.getPlayerId(),
                p.getNickname(),
                payload.absoluteFood(),
                p.getPrestigePoints(),
                p.getTotemColor(),
                newCharacters,
                newBuildings
        );
    }

    private List<PlayerState> applyPlayerDeltas(List<PlayerState> players, List<PlayerDelta> deltas) {
        Map<String, PlayerDelta> deltaMap = new HashMap<>();
        for (PlayerDelta d : deltas) deltaMap.put(d.playerId(), d);
        return players.stream().map(p -> {
            PlayerDelta d = deltaMap.get(p.getPlayerId());
            if (d == null) return p;
            return new PlayerState(p.getPlayerId(), p.getNickname(),
                    d.absoluteFood(),
                    d.absolutePrestige(),
                    p.getTotemColor(), p.getCharacters(), p.getBuildings());
        }).toList();
    }

    private List<CardState> removeCardsById(List<CardState> cards, List<String> cardIds, List<String> buildingIds) {
        Set<String> toRemove = new HashSet<>(cardIds);
        toRemove.addAll(buildingIds);
        return cards.stream().filter(c -> !toRemove.contains(c.getCardId())).toList();
    }

    private List<TurnOrderSlotState> buildTurnOrderSlots(List<String> playerOrder) {
        List<TurnOrderSlotState> slots = new ArrayList<>();
        for (int i = 0; i < playerOrder.size(); i++) {
            slots.add(new TurnOrderSlotState(i, 0, playerOrder.get(i)));
        }
        return slots;
    }

    private List<TurnOrderSlotState> updateTurnOrderSlot(List<TurnOrderSlotState> slots, int slotIndex, String playerId) {
        return slots.stream().map(s -> s.getPositionIndex() == slotIndex
                ? new TurnOrderSlotState(slotIndex, s.getFoodDelta(), playerId)
                : s).toList();
    }

    private BoardState rebuildBoard(BoardState original,
                                    List<CardState> topRow, List<CardState> bottomRow,
                                    List<CardState> topBuildings, List<CardState> bottomBuildings,
                                    List<OfferTileState> offerTiles, List<TurnOrderSlotState> turnOrderSlots) {
        return new BoardState(topRow, bottomRow, topBuildings, bottomBuildings, offerTiles, turnOrderSlots);
    }

    private GameState rebuildWithBoard(GameState gs, BoardState newBoard) {
        return new GameState(gs.getCurrentEra(), gs.getCurrentRound(), gs.getPhase(),
                gs.getCurrentPlayerId(), gs.getPlayers(), newBoard, gs.getSkipAllowed());
    }

    private GameState rebuildWithPlayers(GameState gs, List<PlayerState> newPlayers) {
        return new GameState(gs.getCurrentEra(), gs.getCurrentRound(), gs.getPhase(),
                gs.getCurrentPlayerId(), newPlayers, gs.getBoard(), gs.getSkipAllowed());
    }

    private GameState rebuildWithBoardAndPlayers(GameState gs, BoardState newBoard, List<PlayerState> newPlayers) {
        return new GameState(gs.getCurrentEra(), gs.getCurrentRound(), gs.getPhase(),
                gs.getCurrentPlayerId(), newPlayers, newBoard, gs.getSkipAllowed());
    }

    // handling
    private boolean handleCommand(String line) throws Exception {
        String[] tokens = line.split("\\s+");
        String command = tokens[0].toLowerCase();

        if (!"peek".equals(command)) {
            currentPeekedCard = null;
        }

        return switch (command) {
            case "?", "help", "info", "lobbies" -> false;
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
                    server.createLobby(playerId, lobbyName, DEFAULT_LOBBY_MAX_PLAYERS);
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
                } else {
                    server.placeTotem(playerId, tokens[1].charAt(0));
                }
                yield false;
            }
            case "take" -> {
                ensureConnected();
                if (tokens.length < 4) {
                    printWarning("Use: take <top|bottom> <index> <card|building>");
                } else {
                    String rowStr = tokens[1].toLowerCase();
                    int boardIndex = Integer.parseInt(tokens[2]);
                    boolean isBuilding = tokens[3].equalsIgnoreCase("building");
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
                } else {
                    int index = Integer.parseInt(tokens[1]);
                    boolean isBuilding = tokens.length >= 3 && tokens[2].equalsIgnoreCase("building");
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
                        // best effort shutdown
                    }
                }
                yield true;
            }
            default -> {
                renderCurrentScreen(WARNING_MARKER + " Unknown command. Type 'help'.");
                yield false;
            }
        };
    }

    private void ensureConnected() {
        if (playerId == null) {
            throw new IllegalStateException(connectError != null ? connectError : "Client not connected");
        }
    }

    private boolean containsPlayer(LobbyState lobby, String playerId) {
        return lobby.getPlayers().stream().anyMatch(player -> Objects.equals(player.getId(), playerId));
    }

    private void renderGameView(GameState gameState, String message) {
        clearScreen();
        boardRenderer.render(gameState, message, playerId);
        if (currentPeekedCard != null) {
            renderCardDetails(currentPeekedCard);
        }
    }

    private void renderLobbyView(String message) {
        clearScreen();
        preLobbyRenderer.render(playerName, playerId, lobbies, message, currentLobbyId);
    }

    private void renderCurrentScreen(String message) {
        if (currentGameState != null) {
            renderGameView(currentGameState, message);
            return;
        }
        renderLobbyView(message);
    }

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

        BoardState board = currentGameState.getBoard();
        List<? extends CardState> cards;
        if (board == null) {
            printMuted("Board unavailable.");
            return;
        }

        switch (group) {
            case "TOP_CARDS" -> cards = board.getTopRow();
            case "BOTTOM_CARDS" -> cards = board.getBottomRow();
            case "TOP_BUILDINGS" -> cards = board.getTopBuildings();
            case "BOTTOM_BUILDINGS" -> cards = board.getBottomBuildings();
            default -> {
                printMuted("Invalid selection.");
                return;
            }
        }

        if (cards.isEmpty()) {
            printMuted("Empty");
            return;
        }

        // card selection
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

    private CardState findCardById(String cardId) {
        BoardState board = currentGameState.getBoard();
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

    private List<CardState> getAllBoardCards(BoardState board) {
        List<CardState> cards = new ArrayList<>();
        cards.addAll(board.getTopRow());
        cards.addAll(board.getBottomRow());
        cards.addAll(board.getTopBuildings());
        cards.addAll(board.getBottomBuildings());
        return cards;
    }

    private void displayCardDetails(CardState card) {
        this.currentPeekedCard = card;
        renderCurrentScreen(INFO_MARKER + " Inspecting card " + safeText(card.getCardId()));
    }

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

    private List<String> buildCardDisplayLines(CardState card) {
        List<String> lines = new ArrayList<>();
        lines.add(centerText("CARD DETAILS", 24));
        lines.add("");
        lines.addAll(centerAsciiArt(36));
        lines.add("");
        lines.add("Id: " + safeText(card.getCardId()));
        lines.add("Kind: " + safeText(String.valueOf(card.getCardKind())));
        lines.add("Era: " + safeText(String.valueOf(card.getEra())));
        lines.add("Printed points: " + card.getPrintedPoints());

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

    private List<String> centerAsciiArt(int width) {
        return List.of(
                centerText("  /\\", width),
                centerText(" /  \\", width),
                centerText("| [] |", width),
                centerText("|____|", width),
                centerText("  \\/", width)
        );
    }

    private void addOptionalCardLine(List<String> lines, CardState card, String label, String... methodNames) {
        Object value = invokeGetterOptional(card, methodNames);
        if (value != null) {
            lines.add(label + ": " + value);
        }
    }

    private void printCardBorder(int width) {
        System.out.println("+" + "-".repeat(Math.max(0, width - 2)) + "+");
    }

    private String padRight(String text, int width) {
        String value = text == null ? "" : text;
        if (value.length() >= width) {
            return value;
        }
        return value + " ".repeat(width - value.length());
    }

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

    private Object invokeGetterOptional(Object target, String... methodNames) {
        try {
            for (String methodName : methodNames) {
                try {
                    Method m = target.getClass().getMethod(methodName);
                    return m.invoke(target);
                } catch (NoSuchMethodException ignored) {
                    // try next candidate
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private void clearScreen() {
        System.out.print("\u001B[H\u001B[2J\u001B[3J");
        System.out.flush();
    }

    private void printWarning(String message) {
        System.out.println(WARNING_MARKER + " WARN " + message);
    }

    private void printMuted(String message) {
        System.out.println(message);
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "n/a" : value;
    }

}