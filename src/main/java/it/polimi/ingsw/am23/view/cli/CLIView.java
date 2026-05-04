package it.polimi.ingsw.am23.view.cli;

import de.codeshelf.consoleui.prompt.ConsolePrompt;
import de.codeshelf.consoleui.prompt.InputResult;
import de.codeshelf.consoleui.prompt.ListResult;
import de.codeshelf.consoleui.prompt.builder.PromptBuilder;
import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.enums.RowType;
import it.polimi.ingsw.am23.model.state.BoardState;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.GameState;
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
                            "start", "place", "take", "extra", "state", "quit", "exit"
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
    public synchronized void onGameStarted(GameState gameState) {
        this.currentGameState = gameState;
        gameStartedLatch.countDown();
        renderCurrentScreen(SUCCESS_MARKER + " Game started.");
    }

    @Override
    public synchronized void onGameStateChanged(GameState gameState) {
        this.currentGameState = gameState;
        renderCurrentScreen(INFO_MARKER + " Game state updated.");
    }

    @Override
    public synchronized void onEndOfPlacingPhase(GameState gameState) {
        this.currentGameState = gameState;
        renderCurrentScreen(INFO_MARKER + " End of totem placing phase.");
    }

    @Override
    public synchronized void onEndOfDrawingPhase(GameState gameState) {
        this.currentGameState = gameState;
        renderCurrentScreen(INFO_MARKER + " End of drawing phase.");
    }

    @Override
    public synchronized void onExtraDrawRequest(GameState gameState) {
        this.currentGameState = gameState;
        renderCurrentScreen(WARNING_MARKER + " Requested extra draw for player " + safeText(gameState.getCurrentPlayerId()));
    }

    @Override
    public synchronized void onEndOfResolvingPhase(GameState gameState) {
        this.currentGameState = gameState;
        renderCurrentScreen(INFO_MARKER + " End of resolving phase.");
    }

    @Override
    public synchronized void onEraProgression(GameState gameState) {
        this.currentGameState = gameState;
        renderCurrentScreen(TITLE_MARKER + " New era: " + gameState.getCurrentEra());
    }

    @Override
    public synchronized void onGameOver(GameState gameState) {
        this.currentGameState = gameState;
        renderCurrentScreen(TITLE_MARKER + " Game ended.");
    }

    @Override
    public synchronized void onScoreboardAvailable(GameState gameState) {
        this.currentGameState = gameState;
        renderCurrentScreen(SUCCESS_MARKER + " Scoreboard available.");
    }

    @Override
    public synchronized void onActionError(ActionType actionType, String message) {
        renderCurrentScreen(ERROR_MARKER + " Error " + actionType + ": " + message);
    }

    private boolean handleCommand(String line) throws Exception {
        String[] tokens = line.split("\\s+");
        String command = tokens[0].toLowerCase();

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
                    printWarning("Use: extra <index>");
                } else {
                    server.takeExtraCard(playerId, Integer.parseInt(tokens[1]));
                }
                yield false;
            }
            case "state" -> {
                if (currentGameState == null) {
                    renderCurrentScreen(WARNING_MARKER + " No game state available.");
                } else {
                    renderCurrentScreen(null);
                }
                yield false;
            }
            case "peek" -> {
                ensureConnected();
                if (currentGameState == null) {
                    printWarning("No game state available.");
                } else {
                    handlePeek();
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

    private void handlePeek() throws Exception {
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

    private void displayCardDetails(CardState card) {
        System.out.println();
        System.out.println("--- Card details ---");
        System.out.println("Id: " + safeText(card.getCardId()));
        System.out.println("Kind: " + safeText(card.getCardKind().toString()));
        System.out.println("Era: " + safeText(card.getEra().toString()));
        System.out.println("PrintedPoints: " + card.getPrintedPoints());

        Object o;
        o = invokeGetterOptional(card, "getCharacterType");
        if (o != null) System.out.println("CharacterType: " + o);
        o = invokeGetterOptional(card, "getHasFoodSymbol");
        if (o != null) System.out.println("HasFoodSymbol: " + o);
        o = invokeGetterOptional(card, "getStars");
        if (o != null) System.out.println("Stars: " + o);
        o = invokeGetterOptional(card, "getDiscount");
        if (o != null) System.out.println("Discount: " + o);
        o = invokeGetterOptional(card, "getInventionIcon");
        if (o != null) System.out.println("InventionIcon: " + o);
        o = invokeGetterOptional(card, "getFoodCost");
        if (o != null) System.out.println("FoodCost: " + o);
        o = invokeGetterOptional(card, "getEffectId");
        if (o != null) System.out.println("EffectId: " + o);
        o = invokeGetterOptional(card, "getMinPlayers");
        if (o != null) System.out.println("MinPlayers: " + o);
        System.out.println();
    }

    private Object invokeGetterOptional(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName);
            return m.invoke(target);
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