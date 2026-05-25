package it.polimi.ingsw.am23.view.gui;

import it.polimi.ingsw.am23.model.draw.SelectedCardExtraDraw;
import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;
import it.polimi.ingsw.am23.model.enums.ActionType;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.payloads.*;
import it.polimi.ingsw.am23.model.state.*;
import it.polimi.ingsw.am23.network.LobbyState;
import it.polimi.ingsw.am23.network.NetworkSetter;
import it.polimi.ingsw.am23.network.VirtualServer;
import it.polimi.ingsw.am23.network.VirtualView;
import it.polimi.ingsw.am23.view.gui.controllers.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;

import java.util.*;
import java.util.stream.Collectors;

public class JavaFXView extends Application implements VirtualView {
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

    private final List<EventResolvedPayload> pendingEventPayloads = new ArrayList<>();

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;
        primaryStage.setTitle("MESOS");
        showConnectionScreen();
        primaryStage.show();

        // disconnessine alla chiusura della finestra
        primaryStage.setOnCloseRequest(e -> {
            NetworkSetter.stopHeartbeat();
            if(server != null && playerId != null){
                try{
                    server.disconnect(playerId);
                } catch (Exception ignored) {}
            }
        });
    }

    public static void main(String[] args){
        launch(args);
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

    public void connect(String host, String nickname, String connectionType) throws Exception{
        this.playerName = nickname;
        this.server = NetworkSetter.connect(host, nickname, this, connectionType);
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

    public void joinLobby(String lobbyId){
        try{
            server.joinLobby(playerId, lobbyId);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void createLobby(String name, int maxPlayers){
        try {
            server.createLobby(playerId, name, maxPlayers);
        }catch(Exception e){
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
        waitingRoomController.setLobbyInfo(lobby.getLobbyId(),lobby.getLobbyName(), lobby.getMaxPlayers());
        waitingRoomController.updatePlayerList(lobby.getPlayers().stream().map(player -> player.getNickname()).collect(java.util.stream.Collectors.toList()));
        primaryStage.setScene(new Scene(root, 400, 500));
    }

    public void leaveLobby(){
        try{
            leftVoluntarily = true;
            server.leaveLobby(playerId, currentLobbyId);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void goToLobby(){
        Platform.runLater(()-> {
            try{
                scoreboardController = null;
                gameScreenController = null;
                currentGameState = null;
                returningToLobby = true;
                server.requestLobbyList(playerId);
            }catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    // GAME

    private void showGameScreen(GameState gameState) throws Exception{
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/gameScreen.fxml")
        );
        Parent root = loader.load();
        gameScreenController = loader.getController();
        gameScreenController.setView(this);
        gameScreenController.setMyPlayerId(playerId);
        gameScreenController.setPrimaryStage(primaryStage);
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
        try{
            server.placeTotem(playerId, tileId);
        } catch (Exception e){
            String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING
                );
                alert.setTitle("Azione non valida.");
                alert.setHeaderText(null);
                alert.setContentText(msg);
                alert.showAndWait();
            });
        }
    }

    public void takeSingleCard(SelectedSingleCard card){
        try{
            server.takeSingleCard(playerId, card);
        } catch (Exception e){
            String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING
                );
                alert.setTitle("Azione non valida.");
                alert.setHeaderText(null);
                alert.setContentText(msg);
                alert.showAndWait();
            });
        }
    }

    public void takeExtraCard(int index, boolean isTribeCard){
        try{
            SelectedCardExtraDraw selected = isTribeCard
                    ? new SelectedCardExtraDraw(index, null)
                    : new SelectedCardExtraDraw(null, index);
            server.takeExtraCard(playerId, selected);
        } catch (Exception e){
            String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.WARNING
                );
                alert.setTitle("Azione non valida.");
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
                alert.setTitle("Azione non valida.");
                alert.setHeaderText(null);
                alert.setContentText(msg);
                alert.showAndWait();
            });
        }
    }

    // -----------------

    @Override
    public void onConnected(String playerId, List<LobbyState> lobbies) throws Exception {
        this.playerId = playerId;
        try{ server.requestLobbyList(playerId);
        } catch (Exception ignored) {}

        Platform.runLater(() -> {
            try {
                showLobbyScreen(lobbies);
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onConnectError(String reason) throws Exception {
        Platform.runLater(()->{
            if (connectionController != null) {
                connectionController.showError(reason);
            }
        });
    }

    @Override
    public void onLobbyListUpdated(List<LobbyState> lobbies) throws Exception {
        Platform.runLater(()-> {
            if(returningToLobby){
                returningToLobby = false;
                try{
                    showLobbyScreen(lobbies);
                } catch (Exception e){
                    e.printStackTrace();
                }
            } else if(lobbyController != null) {
                lobbyController.updateLobbies(lobbies);
            }
        });
    }

    @Override
    public void onLobbyCreated(LobbyState lobby) throws Exception {
        this.currentLobbyId = lobby.getLobbyId();
        this.owner = true;
        Platform.runLater(()-> {
            try{
                showWaitingRoomScreen(lobby);
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onLobbyUpdate(LobbyState lobby) throws Exception {
        Platform.runLater(()-> {
            try{
                // se già in waiting room aggiorna lista players
                if(waitingRoomController != null){
                    waitingRoomController.updatePlayerList(
                            lobby.getPlayers().stream()
                                    .map(player -> player.getNickname())
                                    .collect(java.util.stream.Collectors.toList())
                    );
                }else{ // chi fa join entra nella waiting room
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
    public void onJoinError(String reason) throws Exception {
        Platform.runLater(() -> {
            if(lobbyController != null){
                lobbyController.showError(reason);
            }
        });
    }

    @Override
    public void onLobbyClosed() throws Exception {
        Platform.runLater(()->{
            try {
                waitingRoomController = null;
                showLobbyScreen(java.util.List.of());
                if (!leftVoluntarily){
                    lobbyController.showError("La lobby è stata chiusa. Scegliene una nuova.");
                }
                leftVoluntarily = false;
                server.requestLobbyList(playerId);
            }catch (Exception e){
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
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    @Override
    public void onTotemPlaced(TotemPlacedPayload payload) throws Exception {
        if (currentGameState == null) {
            return;
        }

        BoardState board = currentGameState.getBoard();

        List<OfferTileState> updatedTiles = board.getOfferTiles().stream()
                .map(t -> t.getTileId() == payload.offerTileChar()
                        ? new OfferTileState(
                        t.getPositionIndex(),
                        t.getTileId(),
                        payload.playerId(),
                        t.getMinPlayers(),
                        t.getTopDrawCount(),
                        t.getBottomDrawCount(),
                        t.getFoodReward()
                )
                        : t)
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
        BoardState board = currentGameState.getBoard();
        List<TurnOrderSlotState> emptySlots = board.getTurnOrderSlots().stream()
                .map(s -> new TurnOrderSlotState(s.getPositionIndex(), s.getFoodDelta(), null))
                .toList();
        currentGameState = new GameState(
                currentGameState.getCurrentEra(), currentGameState.getCurrentRound(),
                GamePhase.RESOLVING_OFFERS,
                payload.firstPlayerId(),
                currentGameState.getPlayers(),
                rebuildBoard(board, board.getTopRow(), board.getBottomRow(),
                        board.getTopBuildings(), board.getBottomBuildings(),
                        board.getOfferTiles(), emptySlots),
                payload.skipAllowed()
        );
        final GameState snap = currentGameState;
        Platform.runLater(() -> { if (gameScreenController != null) gameScreenController.updateGameState(snap); });
    }

    @Override
    public void onCardsTaken(CardsTakenPayload payload) throws Exception {
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
                .map(p -> p.getPlayerId().equals(payload.playerId()) ? applyCardDeltaToPlayer(p, payload) : p)
                .toList();

        // aggiorna fase e currentPlayer dai nuovi campi del payload
        currentGameState = new GameState(
                currentGameState.getCurrentEra(), currentGameState.getCurrentRound(),
                payload.newPhase(),
                payload.nextPlayerId(),
                updatedPlayers,
                newBoard,
                payload.skipAllowed()
        );
        final GameState snap = currentGameState;
        Platform.runLater(() -> { if (gameScreenController != null) gameScreenController.updateGameState(snap); });
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

        BoardState board = currentGameState.getBoard();
        List<String> id = List.of(payload.cardId());

        BoardState newBoard = rebuildBoard(
                board,
                removeCardsById(board.getTopRow(), id, List.of()),
                removeCardsById(board.getBottomRow(), id, List.of()),
                removeCardsById(board.getTopBuildings(), List.of(), id),
                removeCardsById(board.getBottomBuildings(), List.of(), id),
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
        List<PlayerState> updatedPlayers = applyPlayerDeltas(currentGameState.getPlayers(), payload.playerDeltas());
        currentGameState = rebuildWithPlayers(currentGameState, updatedPlayers);
        pendingEventPayloads.add(payload);
        final GameState snap = currentGameState;
        Platform.runLater(() -> { if (gameScreenController != null) gameScreenController.updateGameState(snap); });
    }

    @Override
    public void onMarketRefreshed(MarketRefresherPayload payload) throws Exception {
        if (currentGameState == null) return;
        BoardState board = currentGameState.getBoard();

        List<CardState> newBottom = new ArrayList<>(
                board.getBottomRow().stream()
                        .filter(c -> !payload.discardedCardIds().contains(c.getCardId()))
                        .toList());
        board.getTopRow().stream()
                .filter(c -> payload.movedBottomCardIds().contains(c.getCardId()))
                .forEach(newBottom::add);
        List<CardState> newTop = new ArrayList<>(board.getTopRow().stream()
                .filter(c -> !payload.movedBottomCardIds().contains(c.getCardId()))
                .toList());
        newTop.addAll(payload.newUpperRowCards());

        // aggiorna anche fase e round
        currentGameState = new GameState(
                currentGameState.getCurrentEra(),
                payload.newRound(),
                payload.newPhase(),
                payload.nextPlayerId(),
                currentGameState.getPlayers(),
                rebuildBoard(
                        board,
                        newTop,
                        newBottom,
                        board.getTopBuildings(),
                        board.getBottomBuildings(),
                        payload.offerTiles(),
                        payload.turnOrderSlots()
                ),
                payload.skipAllowed()
        );
        final GameState snap = currentGameState;
        final List<EventResolvedPayload> events = List.copyOf(pendingEventPayloads);
        final List<PlayerState> players = List.copyOf(snap.getPlayers());
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
        final GameState snap = currentGameState;
        Platform.runLater(() -> { if (gameScreenController != null) gameScreenController.updateGameState(snap); });
    }

    @Override
    public void onGameOver() throws Exception {
        final GameState snap = currentGameState;
        Platform.runLater(() -> { if (gameScreenController != null) gameScreenController.updateGameState(snap); });
    }

    @Override
    public void onScoreboardAvailable(ScoreBoardPayload payload) throws Exception {
        Platform.runLater(() -> {
            try {
                gameScreenController = null;
                showScoreboardFromPayload(payload);
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    private void showScoreboardFromPayload(ScoreBoardPayload payload) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/scoreboard.fxml"));
        Parent root = loader.load();
        scoreboardController = loader.getController();
        scoreboardController.setView(this);
        scoreboardController.showScoreboard(payload);
        primaryStage.getScene().setRoot(root);
    }


    @Override
    public void onActionError(ActionType actionType, String message) throws Exception {
        Platform.runLater( () -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.WARNING
        );
            alert.setTitle("Azione non valida");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Override
    public void onServerCrashed(){
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
            alert.setTitle("Connessione persa.");
            alert.setHeaderText("Server non raggiungibile.");
            alert.setContentText("Tornerai alla schermata di connessione.");
            alert.showAndWait();

            try{
                showConnectionScreen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // -----------
    private PlayerState applyCardDeltaToPlayer(PlayerState p, CardsTakenPayload payload) {
        List<CardState> newCharacters = new ArrayList<>(p.getCharacters());
        Set<String> existingCharacterIds = newCharacters.stream()
                .map(CardState::getCardId)
                .collect(Collectors.toSet());

        for (CardState card : payload.takenCards()) {
            if (existingCharacterIds.add(card.getCardId())) {
                newCharacters.add(card);
            }
        }

        List<CardState> newBuildings = new ArrayList<>(p.getBuildings());
        Set<String> existingBuildingIds = newBuildings.stream()
                .map(CardState::getCardId)
                .collect(Collectors.toSet());

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
                    p.getTotemColor(), p.getCharacters(), p.getBuildings());}).toList();
    }

    private List<CardState> removeCardsById(List<CardState> cards, List<String> cardIds, List<String> buildingIds) {
        Set<String> toRemove = new HashSet<>(cardIds);
        toRemove.addAll(buildingIds);
        return cards.stream().filter(c -> !toRemove.contains(c.getCardId())).toList();
    }

    private List<TurnOrderSlotState> buildTurnOrderSlots(List<String> playerOrder) {
        List<TurnOrderSlotState> slots = new ArrayList<>();
        for (int i = 0; i < playerOrder.size(); i++) slots.add(new TurnOrderSlotState(i, 0, playerOrder.get(i)));
        return slots;
    }

    private List<TurnOrderSlotState> updateTurnOrderSlot(List<TurnOrderSlotState> slots, int slotIndex, String playerId) {
        return slots.stream().map(s -> s.getPositionIndex() == slotIndex
                ? new TurnOrderSlotState(slotIndex, s.getFoodDelta(), playerId) : s).toList();
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
}
