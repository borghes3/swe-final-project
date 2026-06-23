package it.polimi.ingsw.am23.view.gui;

import it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;
import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.payloads.*;
import it.polimi.ingsw.am23.model.setup.PlayerConnectionInfo;
import it.polimi.ingsw.am23.model.state.*;
import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.NetworkSetter;
import it.polimi.ingsw.am23.network.VirtualServer;
import it.polimi.ingsw.am23.network.VirtualView;
import it.polimi.ingsw.am23.view.ClientArgs;
import it.polimi.ingsw.am23.view.gui.controllers.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class JavaFXView extends Application implements VirtualView {
    private final List<EventResolvedPayload> pendingEventPayloads = new ArrayList<>();
    private volatile VirtualServer server;
    private volatile String playerId;
    private volatile String playerName;
    private volatile String currentLobbyId;
    private volatile boolean owner;
    private volatile boolean leftVoluntarily = false;
    private volatile GameState currentGameState;
    private volatile boolean returningToLobby = false;
    private Stage primaryStage;
    private ConnectionController connectionController;
    private LobbyController lobbyController;
    private WaitingRoomController waitingRoomController;
    private GameScreenController gameScreenController;
    private ScoreboardController scoreboardController;
    private volatile int lastMatchPlayerCount = -1;
    private int rmiCallbackPort;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        this.rmiCallbackPort = ClientArgs.parseRmiCallbackPort(getParameters().getRaw());
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        primaryStage.setTitle("MESOS");
        showConnectionScreen();
        primaryStage.show();

        // disconnessine alla chiusura della finestra
        primaryStage.setOnCloseRequest(e -> {
            NetworkSetter.stopHeartbeat();
            if (server != null && playerId != null) {
                try {
                    server.disconnect(playerId);
                } catch (Exception ignored) {
                }
            }
        });
    }

    // CONNECTION

    private void showConnectionScreen() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/connection.fxml")
        );
        Parent root = loader.load();
        connectionController = loader.getController();
        connectionController.setView(this);
        primaryStage.setScene(new Scene(root, 400, 500));
    }

    public void connect(String host, String nickname, String connectionType) throws Exception {
        this.playerName = nickname;
        this.server = NetworkSetter.connect(host, nickname, this, connectionType, rmiCallbackPort);
    }

    // LOBBY

    private void showLobbyScreen(List<LobbyState> lobbies) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/lobby.fxml")
        );
        Parent root = loader.load();
        lobbyController = loader.getController();
        lobbyController.setView(this);
        lobbyController.updateLobbies(lobbies);
        primaryStage.setScene(new Scene(root, 400, 500));
    }

    public void joinLobby(String lobbyId) {
        try {
            server.joinLobby(playerId, lobbyId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createLobby(String name, int maxPlayers) {
        try {
            server.createLobby(playerId, name, maxPlayers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // WAITING ROOM

    private void showWaitingRoomScreen(LobbyState lobby) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/waitingRoom.fxml")
        );
        Parent root = loader.load();
        waitingRoomController = loader.getController();
        waitingRoomController.setView(this);
        waitingRoomController.setOwner(owner);
        waitingRoomController.setLobbyInfo(lobby.getLobbyId(), lobby.getLobbyName(), lobby.getMaxPlayers());
        waitingRoomController.updatePlayerList(lobby.getPlayers().stream().map(PlayerConnectionInfo::nickname).collect(java.util.stream.Collectors.toList()));
        primaryStage.setScene(new Scene(root, 400, 500));
    }

    public void leaveLobby() {
        try {
            leftVoluntarily = true;
            server.leaveLobby(playerId, currentLobbyId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToLobby() {
        Platform.runLater(() -> {
            try {
                scoreboardController = null;
                gameScreenController = null;
                currentGameState = null;
                returningToLobby = true;
                server.requestLobbyList(playerId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // GAME

    private void showGameScreen(GameState gameState) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/gameScreen.fxml")
        );
        Parent root = loader.load();
        gameScreenController = loader.getController();
        gameScreenController.setView(this);
        gameScreenController.setMyPlayerId(playerId);
        gameScreenController.setPrimaryStage();
        gameScreenController.updateGameState(gameState);
        primaryStage.setScene(new Scene(root, 1100, 800));
    }

    public void startGame() {
        try {
            server.startGame(playerId, currentLobbyId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void placeTotem(char tileId) {
        try {
            server.placeTotem(playerId, tileId);
        } catch (Exception e) {
            String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING
                );
                alert.setTitle("Invalid action.");
                alert.setHeaderText(null);
                alert.setContentText(msg);
                alert.showAndWait();
            });
        }
    }

    public void takeSingleCard(SelectedSingleCard card) {
        try {
            server.takeSingleCard(playerId, card);
        } catch (Exception e) {
            String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING
                );
                alert.setTitle("Invalid action.");
                alert.setHeaderText(null);
                alert.setContentText(msg);
                alert.showAndWait();
            });
        }
    }

    public void takeExtraCard(int index, boolean isTribeCard) {
        try {
            SelectedCardExtraDraw selected = isTribeCard
                    ? new SelectedCardExtraDraw(index, null)
                    : new SelectedCardExtraDraw(null, index);
            server.takeExtraCard(playerId, selected);
        } catch (Exception e) {
            String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING
                );
                alert.setTitle("Invalid action.");
                alert.setHeaderText(null);
                alert.setContentText(msg);
                alert.showAndWait();
            });
        }
    }

    public void skipTurn() {
        try {
            server.skipTurn(playerId);
        } catch (Exception e) {
            String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING
                );
                alert.setTitle("Invalid action.");
                alert.setHeaderText(null);
                alert.setContentText(msg);
                alert.showAndWait();
            });
        }
    }

    // -----------------

    @Override
    public void onConnected(String playerId, List<LobbyState> lobbies) {
        this.playerId = playerId;
        try {
            server.requestLobbyList(playerId);
        } catch (Exception ignored) {
        }

        Platform.runLater(() -> {
            try {
                showLobbyScreen(lobbies);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onConnectError(String reason) {
        Platform.runLater(() -> {
            if (connectionController != null) {
                connectionController.showError(reason);
            }
        });
    }

    @Override
    public void onLobbyListUpdated(List<LobbyState> lobbies) {
        Platform.runLater(() -> {
            if (returningToLobby) {
                returningToLobby = false;
                try {
                    showLobbyScreen(lobbies);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (lobbyController != null) {
                lobbyController.updateLobbies(lobbies);
            }
        });
    }

    @Override
    public void onLobbyCreated(LobbyState lobby) {
        this.currentLobbyId = lobby.getLobbyId();
        this.owner = true;
        Platform.runLater(() -> {
            try {
                showWaitingRoomScreen(lobby);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onLobbyUpdate(LobbyState lobby) {
        Platform.runLater(() -> {
            try {
                // se già in waiting room aggiorna lista players
                if (waitingRoomController != null) {
                    waitingRoomController.updatePlayerList(
                            lobby.getPlayers().stream()
                                    .map(PlayerConnectionInfo::nickname)
                                    .collect(java.util.stream.Collectors.toList())
                    );
                } else { // chi fa join entra nella waiting room
                    this.currentLobbyId = lobby.getLobbyId();
                    this.owner = false;
                    showWaitingRoomScreen(lobby);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onJoinError(String reason) {
        Platform.runLater(() -> {
            if (lobbyController != null) {
                lobbyController.showError(reason);
            }
        });
    }

    @Override
    public void onLobbyClosed() {
        Platform.runLater(() -> {
            try {
                waitingRoomController = null;
                showLobbyScreen(java.util.List.of());
                if (!leftVoluntarily) {
                    lobbyController.showError("The lobby has been closed. Choose a new one.");
                }
                leftVoluntarily = false;
                server.requestLobbyList(playerId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onGameStarted(GameStartedPayload payload) throws Exception {
        this.currentGameState = payload.fullSnapshot();
        Platform.runLater(() -> {
            try {
                waitingRoomController = null;
                showGameScreen(currentGameState);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onTotemPlaced(TotemPlacedPayload payload) throws Exception {
        if (currentGameState == null) {
            return;
        }

        BoardState board = currentGameState.board();

        List<OfferTileState> updatedTiles = board.offerTiles().stream()
                .map(t -> t.tileId() == payload.offerTileChar()
                        ? new OfferTileState(
                        t.positionIndex(),
                        t.tileId(),
                        payload.playerId(),
                        t.minPlayers(),
                        t.topDrawCount(),
                        t.bottomDrawCount(),
                        t.foodReward()
                )
                        : t)
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

        final GameState snap = currentGameState;

        Platform.runLater(() -> {
            if (gameScreenController != null) {
                gameScreenController.updateGameState(snap);
            }
        });
    }

    @Override
    public void onEndOfPlacingPhase(EndOfPlacingPhasePayload payload) throws Exception {
        if (currentGameState == null) return;
        BoardState board = currentGameState.board();
        List<TurnOrderSlotState> emptySlots = board.turnOrderSlots().stream()
                .map(s -> new TurnOrderSlotState(s.positionIndex(), s.foodDelta(), null))
                .toList();
        currentGameState = new GameState(
                currentGameState.currentEra(), currentGameState.currentRound(),
                GamePhase.RESOLVING_OFFERS,
                payload.firstPlayerId(),
                currentGameState.players(),
                rebuildBoard(board.topRow(), board.bottomRow(),
                        board.topBuildings(), board.bottomBuildings(),
                        board.offerTiles(), emptySlots),
                payload.skipAllowed()
        );
        final GameState snap = currentGameState;
        Platform.runLater(() -> {
            if (gameScreenController != null) gameScreenController.updateGameState(snap);
        });
    }

    @Override
    public void onCardsTaken(CardsTakenPayload payload) throws Exception {
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
                .map(p -> p.playerId().equals(payload.playerId()) ? applyCardDeltaToPlayer(p, payload) : p)
                .toList();

        // aggiorna fase e currentPlayer dai nuovi campi del payload
        currentGameState = new GameState(
                currentGameState.currentEra(), currentGameState.currentRound(),
                payload.newPhase(),
                payload.nextPlayerId(),
                updatedPlayers,
                newBoard,
                payload.skipAllowed()
        );
        final GameState snap = currentGameState;
        Platform.runLater(() -> {
            if (gameScreenController != null) gameScreenController.updateGameState(snap);
        });
    }

    @Override
    public void onExtraDrawRequest(ExtraDrawRequestPayload payload) throws Exception {
        if (currentGameState == null) return;
        final GameState snap = currentGameState;
        Platform.runLater(() -> {
            if (gameScreenController != null) {
                gameScreenController.updateGameState(snap);
                if (playerId != null && playerId.equals(payload.pendingPlayerId())) {
                    gameScreenController.showExtraDrawDialog(snap);
                }
            }
        });
    }

    @Override
    public void onExtraCardTaken(ExtraCardTakenPayload payload) throws Exception {
        if (currentGameState == null) {
            return;
        }

        BoardState board = currentGameState.board();
        List<String> id = List.of(payload.cardId());

        BoardState newBoard = rebuildBoard(
                removeCardsById(board.topRow(), id, List.of()),
                removeCardsById(board.bottomRow(), id, List.of()),
                removeCardsById(board.topBuildings(), List.of(), id),
                removeCardsById(board.bottomBuildings(), List.of(), id),
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

        final GameState snap = currentGameState;
        Platform.runLater(() -> {
            if (gameScreenController != null) {
                gameScreenController.updateGameState(snap);
            }
        });
    }

    @Override
    public void onEventResolved(EventResolvedPayload payload) throws Exception {
        if (currentGameState == null) return;
        List<PlayerState> updatedPlayers = applyPlayerDeltas(currentGameState.players(), payload.playerDeltas());
        currentGameState = rebuildWithPlayers(currentGameState, updatedPlayers);
        pendingEventPayloads.add(payload);
        final GameState snap = currentGameState;
        Platform.runLater(() -> {
            if (gameScreenController != null) gameScreenController.updateGameState(snap);
        });
    }

    @Override
    public void onMarketRefreshed(MarketRefresherPayload payload) throws Exception {
        if (currentGameState == null) return;
        BoardState board = currentGameState.board();

        List<CardState> newBottom = new ArrayList<>(
                board.bottomRow().stream()
                        .filter(c -> !payload.discardedCardIds().contains(c.getCardId()))
                        .toList());
        board.topRow().stream()
                .filter(c -> payload.movedBottomCardIds().contains(c.getCardId()))
                .forEach(newBottom::add);
        List<CardState> newTop = new ArrayList<>(board.topRow().stream()
                .filter(c -> !payload.movedBottomCardIds().contains(c.getCardId()))
                .toList());
        newTop.addAll(payload.newUpperRowCards());

        // aggiorna anche fase e round
        currentGameState = new GameState(
                currentGameState.currentEra(),
                payload.newRound(),
                payload.newPhase(),
                payload.nextPlayerId(),
                currentGameState.players(),
                rebuildBoard(
                        newTop,
                        newBottom,
                        board.topBuildings(),
                        board.bottomBuildings(),
                        payload.offerTiles(),
                        payload.turnOrderSlots()
                ),
                payload.skipAllowed()
        );
        final GameState snap = currentGameState;
        final List<EventResolvedPayload> events = List.copyOf(pendingEventPayloads);
        final List<PlayerState> players = List.copyOf(snap.players());
        pendingEventPayloads.clear();

        Platform.runLater(() -> {
            if (gameScreenController != null) {
                if (!events.isEmpty()) {
                    gameScreenController.showEventsResolvedDialog(events, players);
                }
                gameScreenController.updateGameState(snap);
            }
        });
    }

    @Override
    public void onEraProgression(EraProgressionPayload payload) throws Exception {
        if (currentGameState == null) return;
        BoardState board = currentGameState.board();

        // i vecchi topBuildings scendono in bottom (eccetto quelli scartati)
        List<CardState> newBottomBuildings = board.topBuildings().stream()
                .filter(c -> !payload.discardedBuildingIds().contains(c.getCardId()))
                .toList();

        // i nuovi edifici dell'era sostituiscono completamente la top
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
        final GameState snap = currentGameState;
        Platform.runLater(() -> {
            if (gameScreenController != null) gameScreenController.updateGameState(snap);
        });
    }

    @Override
    public void onGameOver() throws Exception {
        final GameState snap = currentGameState;
        Platform.runLater(() -> {
            if (gameScreenController != null) gameScreenController.updateGameState(snap);
        });
    }

    @Override
    public void onScoreboardAvailable(ScoreBoardPayload payload) throws Exception {
        if (payload != null && payload.scores() != null) {
            lastMatchPlayerCount = payload.scores().size();
        }
        Platform.runLater(() -> {
            try {
                gameScreenController = null;
                showScoreboardFromPayload(payload);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onMatchRankingsAvailable(MatchRankingsPayload payload) {
        Platform.runLater(() -> {
            if (scoreboardController != null) {
                scoreboardController.showMatchRankings(playerId, payload);
            }
        });
    }

    @Override
    public void onLeaderboardAvailable(LeaderboardPayload payload) {
        Platform.runLater(() -> {
            try {
                showLeaderboardFromPayload(payload);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void requestLeaderboard(int playerCount) {
        try {
            if (server != null && playerId != null) {
                server.requestLeaderboard(playerId, playerCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showScoreboardFromPayload(ScoreBoardPayload payload) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/scoreboard.fxml"));
        Parent root = loader.load();
        scoreboardController = loader.getController();
        scoreboardController.setView(this);
        scoreboardController.showScoreboard(payload);
        primaryStage.getScene().setRoot(root);
    }

    private void showLeaderboardFromPayload(LeaderboardPayload payload) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/leaderboard.fxml"));
        Parent root = loader.load();
        LeaderboardController leaderboardController = loader.getController();
        leaderboardController.setView(this);
        leaderboardController.showLeaderboard(payload, playerName);
        primaryStage.getScene().setRoot(root);
    }

    public void backToScoreboard() {
        Platform.runLater(() -> {
            if (scoreboardController != null && scoreboardController.getRoot() != null) {
                primaryStage.getScene().setRoot(scoreboardController.getRoot());
            }
        });
    }


    @Override
    public void onActionError(ActionType actionType, String message) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING
            );
            alert.setTitle("Invalid action.");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Override
    public void onServerCrashed() {
        NetworkSetter.stopHeartbeat();
        Platform.runLater(() -> {
            server = null;
            playerId = null;
            currentLobbyId = null;
            owner = false;
            leftVoluntarily = false;
            waitingRoomController = null;
            gameScreenController = null;
            scoreboardController = null;

            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    Alert.AlertType.ERROR
            );
            alert.setTitle("Connection lost.");
            alert.setHeaderText("Server unreachable.");
            alert.setContentText("You will be returned to the connection screen.");
            alert.showAndWait();

            try {
                showConnectionScreen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // -----------
    private PlayerState applyCardDeltaToPlayer(PlayerState p, CardsTakenPayload payload) {
        List<CardState> newCharacters = new ArrayList<>(p.characters());
        Set<String> existingCharacterIds = newCharacters.stream()
                .map(CardState::getCardId)
                .collect(Collectors.toSet());

        for (CardState card : payload.takenCards()) {
            if (existingCharacterIds.add(card.getCardId())) {
                newCharacters.add(card);
            }
        }

        List<CardState> newBuildings = new ArrayList<>(p.buildings());
        Set<String> existingBuildingIds = newBuildings.stream()
                .map(CardState::getCardId)
                .collect(Collectors.toSet());

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

    private List<CardState> removeCardsById(List<CardState> cards, List<String> cardIds, List<String> buildingIds) {
        Set<String> toRemove = new HashSet<>(cardIds);
        toRemove.addAll(buildingIds);
        return cards.stream().filter(c -> !toRemove.contains(c.getCardId())).toList();
    }

    private List<TurnOrderSlotState> updateTurnOrderSlot(List<TurnOrderSlotState> slots, int slotIndex, String playerId) {
        return slots.stream().map(s -> s.positionIndex() == slotIndex
                ? new TurnOrderSlotState(slotIndex, s.foodDelta(), playerId) : s).toList();
    }

    private BoardState rebuildBoard(List<CardState> topRow, List<CardState> bottomRow,
                                    List<CardState> topBuildings, List<CardState> bottomBuildings,
                                    List<OfferTileState> offerTiles, List<TurnOrderSlotState> turnOrderSlots) {
        return new BoardState(topRow, bottomRow, topBuildings, bottomBuildings, offerTiles, turnOrderSlots);
    }

    private GameState rebuildWithPlayers(GameState gs, List<PlayerState> newPlayers) {
        return new GameState(gs.currentEra(), gs.currentRound(), gs.phase(),
                gs.currentPlayerId(), newPlayers, gs.board(), gs.skipAllowed());
    }

}
