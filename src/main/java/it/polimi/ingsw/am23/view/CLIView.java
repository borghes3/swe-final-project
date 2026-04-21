package it.polimi.ingsw.am23.view;

import it.polimi.ingsw.am23.model.cards.SelectedCards;
import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.state.*;
import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.VirtualView;
import it.polimi.ingsw.am23.network.rmi.client.RmiClient;
import it.polimi.ingsw.am23.network.rmi.client.VirtualServerRmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public final class CLIView implements VirtualView {

    private static final int DEFAULT_LOBBY_MAX_PLAYERS = 5;

    private final Scanner scanner;
    private final CountDownLatch connectedLatch = new CountDownLatch(1);
    private final CountDownLatch gameStartedLatch = new CountDownLatch(1);

    private volatile VirtualServerRmi server;
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
        view.connect(host, nickname);
        view.run();
    }

    public void connect(String host, String nickname) throws RemoteException, NotBoundException, InterruptedException {
        this.playerName = Objects.requireNonNull(nickname, "nickname cannot be null").trim();
        this.server = RmiClient.connect(host, playerName, this);
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
            if (handleCommand(line)) {
                return;
            }
        }
    }

    @Override
    public synchronized void onConnected(String playerId, List<LobbyState> lobbies) {
        this.playerId = playerId;
        this.lobbies = List.copyOf(lobbies);
        connectedLatch.countDown();
        System.out.println("Connesso come " + playerName + " [" + playerId + "]");
        printLobbyList();
    }

    @Override
    public synchronized void onConnectError(String reason) {
        this.connectError = reason;
        connectedLatch.countDown();
        System.out.println("Connessione fallita: " + reason);
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
    public synchronized void onGameOver() {
        System.out.println("Partita terminata.");
        if (currentGameState != null) {
            printGameState(currentGameState);
        }
    }

    @Override
    public synchronized void onScoreboardAvailable() {
        System.out.println("Classifica disponibile.");
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
                if (tokens.length < 5) {
                    System.out.println("Uso: take ur=... lr=... ub=... lb=...");
                } else {
                    SelectedCards selectedCards = new SelectedCards(
                            parseIndexList(tokens[1]),
                            parseIndexList(tokens[2]),
                            parseIndexList(tokens[3]),
                            parseIndexList(tokens[4])
                    );
                    server.takeCards(playerId, selectedCards);
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
            case "quit", "exit" -> true;
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
        System.out.println("  take ur=... lr=... ub=... lb=...");
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

    private List<Integer> parseIndexList(String token) {
        int separatorIndex = token.indexOf('=');
        if (separatorIndex < 0 || separatorIndex == token.length() - 1) {
            return List.of();
        }
        String rawValues = token.substring(separatorIndex + 1).trim();
        if (rawValues.isEmpty() || rawValues.equals("-")) {
            return List.of();
        }
        String[] parts = rawValues.split(",");
        List<Integer> indices = new ArrayList<>(parts.length);
        for (String part : parts) {
            indices.add(Integer.parseInt(part.trim()));
        }
        return indices;
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