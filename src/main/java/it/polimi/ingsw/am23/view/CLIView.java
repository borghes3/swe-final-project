package it.polimi.ingsw.am23.view;

import it.polimi.ingsw.am23.model.cards.SelectedSingleCard;
import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.enums.RowType;
import it.polimi.ingsw.am23.model.state.*;
import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.NetworkSetter;
import it.polimi.ingsw.am23.network.VirtualServer;
import it.polimi.ingsw.am23.network.VirtualView;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public final class CLIView implements VirtualView {

    private static final int DEFAULT_LOBBY_MAX_PLAYERS = 5;

    private final Scanner scanner;
    private final CountDownLatch connectedLatch = new CountDownLatch(1);
    private final CountDownLatch gameStartedLatch = new CountDownLatch(1);

    private volatile VirtualServer server;
    private volatile String playerId;
    private volatile String playerName;
    private volatile String currentLobbyId;
    private volatile boolean owner;
    private volatile boolean gameStarted;
    private volatile String connectError;
    private volatile List<LobbyState> lobbies = List.of();
    private volatile GameState currentGameState;

    public CLIView() {
        this(new Scanner(System.in));
    }

    public CLIView(Scanner scanner) {
        this.scanner = Objects.requireNonNull(scanner, "scanner cannot be null");
    }

    public static void main(String[] args) throws Exception {
        CLIView view = new CLIView();
        String host = args.length > 0 ? args[0] : view.prompt("Host [localhost]: ", "localhost");
        String nickname = args.length > 1 ? args[1] : view.prompt("Nickname: ", null);
        String normalizedConnectionType = askConnectionType(view, args);
        view.connect(host, nickname, normalizedConnectionType);
        view.run();
    }
    private static String askConnectionType(CLIView view, String[] args){
        if (args.length > 2) {
            return normalizeConnectionType(args[2]);
        }
        while (true) {
            String input = view.prompt("Tipo di connessione [RMI/SOCKET] (default RMI): ", "RMI");
            try {
                return normalizeConnectionType(input);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static String normalizeConnectionType(String connectionType) {
        if (connectionType == null || connectionType.isBlank()) {
            return "RMI";
        }
        return switch (connectionType.trim().toUpperCase()) {
            case "RMI" -> "RMI";
            case "SOCKET" -> "SOCKET";
            default -> throw new IllegalArgumentException("Tipo di connessione non valido: " + connectionType + ". Usare RMI o SOCKET.");
        };
    }

    public void connect(String host, String nickname, String connectionType) throws Exception {
        this.playerName = Objects.requireNonNull(nickname, "nickname cannot be null").trim();
        this.server = NetworkSetter.connect(host, playerName, this, connectionType);
        awaitConnected();
    }

    public void run() throws Exception {
        printHelp();
        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                return;
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                if (handleCommand(line)) {
                    return;
                }
            } catch (Exception e) {
                System.out.println("Comando fallito: " + e.getMessage());
            }
        }
    }

    @Override
    public synchronized void onConnected(String playerId, List<LobbyState> lobbies) {
        this.playerId = playerId;
        this.lobbies = List.copyOf(lobbies);
        System.out.println("Connesso come " + playerName + " [" + playerId + "]");
        printLobbyList();
        connectedLatch.countDown();
    }

    @Override
    public synchronized void onConnectError(String reason) {
        this.connectError = reason;
        System.out.println("Connessione fallita: " + reason);
        connectedLatch.countDown();
    }

    @Override
    public synchronized void onLobbyListUpdated(List<LobbyState> lobbies) {
        this.lobbies = List.copyOf(lobbies);
        System.out.println("Lista lobby aggiornata.");
        printLobbyList();
    }

    @Override
    public synchronized void onLobbyCreated(LobbyState lobby) {
        this.currentLobbyId = lobby.getLobbyId();
        this.owner = true;
        System.out.println("Lobby creata: " + formatLobby(lobby));
    }

    @Override
    public synchronized void onLobbyUpdate(LobbyState lobby) {
        if (containsPlayer(lobby, playerId)) {
            this.currentLobbyId = lobby.getLobbyId();
            this.owner = Objects.equals(lobby.getOwnerPlayerId(), playerId);
        }
        System.out.println("Lobby aggiornata: " + formatLobby(lobby));
    }

    @Override
    public synchronized void onJoinError(String reason) {
        System.out.println("Join fallito: " + reason);
    }

    @Override
    public synchronized void onLobbyClosed() {
        this.currentLobbyId = null;
        this.owner = false;
        System.out.println("La lobby e' stata chiusa.");
    }

    @Override
    public synchronized void onGameStarted(GameState gameState) {
        this.gameStarted = true;
        this.currentGameState = gameState;
        gameStartedLatch.countDown();
        System.out.println("Partita avviata.");
        printGameState(gameState);
    }

    @Override
    public synchronized void onGameStateChanged(GameState gameState) {
        this.currentGameState = gameState;
        System.out.println("Stato partita aggiornato.");
        printGameState(gameState);
    }

    @Override
    public synchronized void onEndOfPlacingPhase(GameState gameState) {
        this.currentGameState = gameState;
        System.out.println("Fine fase piazzamento totem.");
        printGameState(gameState);
    }

    @Override
    public synchronized void onEndOfDrawingPhase(GameState gameState) {
        this.currentGameState = gameState;
        System.out.println("Fine fase pesca.");
        printGameState(gameState);
    }

    @Override
    public synchronized void onExtraDrawRequest(GameState gameState) {
        this.currentGameState = gameState;
        System.out.println("Richiesta pesca extra per " + gameState.getCurrentPlayerId());
        printGameState(gameState);
    }

    @Override
    public synchronized void onEndOfResolvingPhase(GameState gameState) {
        this.currentGameState = gameState;
        System.out.println("Fine fase risoluzione.");
        printGameState(gameState);
    }

    @Override
    public synchronized void onEraProgression(GameState gameState) {
        this.currentGameState = gameState;
        System.out.println("Nuova era: " + gameState.getCurrentEra());
        printGameState(gameState);
    }

    @Override
    public synchronized void onGameOver(GameState gameState) { // RIMUOVERE GameState gameState PER PAYLOADS
        System.out.println("Partita terminata.");
        if (currentGameState != null) {
            printGameState(currentGameState);
        }
    }

    /*@Override - VERSIONE PAYLOADS
    public synchronized void onScoreboardAvailable() {
        System.out.println("Classifica disponibile.");
    }*/

    @Override
    public synchronized void onScoreboardAvailable(GameState gameState){
        this.currentGameState = gameState;
        System.out.println("Classifica disponibile");
        if(gameState.getScores() != null){
            System.out.println("Punteggi finali: ");
            for(var score : gameState.getScores()){
                System.out.println("- " + score.playerId() + "PP=" + score.prestigePoints() + " cibo=" + score.foodPoints());
            }
        }
    }

    @Override
    public synchronized void onActionError(ActionType actionType, String message) {
        System.out.println("Errore " + actionType + ": " + message);
    }

    private boolean handleCommand(String line) throws Exception {
        String[] tokens = line.split("\\s+");
        String command = tokens[0].toLowerCase();

        return switch (command) {
            case "help" -> {
                printHelp();
                yield false;
            }
            case "lobbies" -> {
                printLobbyList();
                yield false;
            }
            case "create" -> {
                ensureConnected();
                String lobbyName = line.substring(command.length()).trim();
                if (lobbyName.isEmpty()) {
                    System.out.println("Uso: create <nome-lobby>");
                } else {
                    server.createLobby(playerId, lobbyName, DEFAULT_LOBBY_MAX_PLAYERS);
                }
                yield false;
            }
            case "join" -> {
                ensureConnected();
                if (tokens.length < 2) {
                    System.out.println("Uso: join <codice-lobby>");
                } else {
                    server.joinLobby(playerId, tokens[1]);
                }
                yield false;
            }
            case "leave" -> {
                ensureConnected();
                if (currentLobbyId == null) {
                    System.out.println("Non sei in nessuna lobby.");
                } else {
                    server.leaveLobby(playerId, currentLobbyId);
                }
                yield false;
            }
            case "start" -> {
                ensureConnected();
                if (currentLobbyId == null) {
                    System.out.println("Non sei in una lobby.");
                } else if (!owner) {
                    System.out.println("Solo il creatore puo' avviare la partita.");
                } else {
                    server.startGame(playerId, currentLobbyId);
                }
                yield false;
            }
            case "place" -> {
                ensureConnected();
                if (tokens.length < 2) {
                    System.out.println("Uso: place <lettera-tile>");
                } else {
                    server.placeTotem(playerId, tokens[1].charAt(0));
                }
                yield false;
            }
            case "take" -> {
                ensureConnected();
                if (tokens.length < 4) {
                    System.out.println("Uso: take <top|bottom> <indice> <card|building>");
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
                    System.out.println("Uso: extra <indice>");
                } else {
                    server.takeExtraCard(playerId, Integer.parseInt(tokens[1]));
                }
                yield false;
            }
            case "state" -> {
                if (currentGameState == null) {
                    System.out.println("Nessuno stato partita disponibile.");
                } else {
                    printGameState(currentGameState);
                }
                yield false;
            }
            case "quit", "exit" -> {
                if (playerId != null && server != null) {
                    try { server.disconnect(playerId); }
                    catch (Exception ignored) {}
                } yield true;
            }
            default -> {
                System.out.println("Comando sconosciuto. Digita 'help'.");
                yield false;
            }
        };
    }

    private void ensureConnected() {
        if (playerId == null) {
            throw new IllegalStateException(connectError != null ? connectError : "Client not connected");
        }
    }

    private void awaitConnected() throws InterruptedException {
        connectedLatch.await();
        if (connectError != null) {
            throw new IllegalStateException(connectError);
        }
    }

    private void printHelp() {
        System.out.println("Comandi disponibili:");
        System.out.println("  help");
        System.out.println("  lobbies");
        System.out.println("  create <nome-lobby>");
        System.out.println("  join <codice-lobby>");
        System.out.println("  leave");
        System.out.println("  start");
        System.out.println("  place <lettera-tile>");
        System.out.println("  take <top|bottom> <indice> <card|building>");
        System.out.println("  extra <indice>");
        System.out.println("  state");
        System.out.println("  quit");
    }

    private void printLobbyList() {
        if (lobbies.isEmpty()) {
            System.out.println("Nessuna lobby disponibile.");
            return;
        }
        System.out.println("Lobby disponibili:");
        for (LobbyState lobby : lobbies) {
            System.out.println("  " + formatLobby(lobby));
        }
    }

    private String formatLobby(LobbyState lobby) {
        return lobby.getLobbyId() + " | " + lobby.getLobbyName() + " | "
                + lobby.getCurrentPlayers() + "/" + lobby.getMaxPlayers() + " giocatori | owner="
                + lobby.getOwnerPlayerId();
    }

    private boolean containsPlayer(LobbyState lobby, String playerId) {
        return lobby.getPlayers().stream().anyMatch(player -> Objects.equals(player.getId(), playerId));
    }

    private void printGameState(GameState gameState) {
        if (gameState == null) {
            System.out.println("Stato partita non disponibile.");
            return;
        }
        System.out.println("=== Game State ===");
        System.out.println("Phase: " + gameState.getPhase() + " | Era: " + gameState.getCurrentEra() + " | Round: " + gameState.getCurrentRound());
        System.out.println("Current player: " + gameState.getCurrentPlayerId());
        System.out.println("Players:");
        for (PlayerState player : gameState.getPlayers()) {
            System.out.println("  - " + player.getNickname() + " [" + player.getPlayerId() + "] food=" + player.getFood()
                    + " pp=" + player.getPrestigePoints() + " totem=" + player.getTotemColor());
        }
        printBoardState(gameState.getBoard());
    }

    private void printBoardState(BoardState board) {
        if (board == null) {
            return;
        }
        System.out.println("Board:");
        System.out.println("  Top row: " + renderCards(board.getTopRow()));
        System.out.println("  Bottom row: " + renderCards(board.getBottomRow()));
        System.out.println("  Top buildings: " + renderCards(board.getTopBuildings()));
        System.out.println("  Bottom buildings: " + renderCards(board.getBottomBuildings()));
        System.out.println("  Offer tiles:");
        for (OfferTileState tile : board.getOfferTiles()) {
            System.out.println("    - " + tile.getTileId() + " owner=" + tile.getOccupiedByPlayerId()
                    + " top=" + tile.getTopDrawCount() + " bottom=" + tile.getBottomDrawCount()
                    + " food=" + tile.getFoodReward());
        }
        System.out.println("  Turn order:");
        for (TurnOrderSlotState slot : board.getTurnOrderSlots()) {
            System.out.println("    - slot " + slot.getPositionIndex() + " foodDelta=" + slot.getFoodDelta()
                    + " player=" + slot.getOccupiedByPlayerId());
        }
    }

    private String renderCards(List<? extends CardState> cards) {
        if (cards == null || cards.isEmpty()) {
            return "[]";
        }
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (CardState card : cards) {
            joiner.add(card.getCardId());
        }
        return joiner.toString();
    }

    private String prompt(String question, String defaultValue) {
        System.out.print(question);
        String value = scanner.nextLine().trim();
        if (value.isEmpty()) {
            return defaultValue == null ? "" : defaultValue;
        }
        return value;
    }
}