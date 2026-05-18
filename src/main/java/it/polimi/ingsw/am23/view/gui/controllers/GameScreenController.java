package it.polimi.ingsw.am23.view.gui.controllers;

import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;
import it.polimi.ingsw.am23.model.enums.CardKind;
import it.polimi.ingsw.am23.model.enums.CharacterType;
import it.polimi.ingsw.am23.model.enums.GamePhase;
import it.polimi.ingsw.am23.model.enums.RowType;
import it.polimi.ingsw.am23.model.enums.TotemColors;
import it.polimi.ingsw.am23.model.payloads.EventResolvedPayload;
import it.polimi.ingsw.am23.model.payloads.PlayerDelta;
import it.polimi.ingsw.am23.model.state.BoardState;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.CharacterCardState;
import it.polimi.ingsw.am23.model.state.GameState;
import it.polimi.ingsw.am23.model.state.OfferTileState;
import it.polimi.ingsw.am23.model.state.PlayerState;
import it.polimi.ingsw.am23.model.state.TurnOrderSlotState;
import it.polimi.ingsw.am23.view.gui.JavaFXView;
import it.polimi.ingsw.am23.view.gui.components.CardNodeFactory;
import it.polimi.ingsw.am23.view.gui.components.OfferTileNodeFactory;
import it.polimi.ingsw.am23.view.gui.components.TurnOrderNodeFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameScreenController {

    @FXML private StackPane rootStack;
    @FXML private ImageView backgroundImageView;

    @FXML private Label eraLabel;
    @FXML private Label roundLabel;
    @FXML private Label phaseLabel;

    @FXML private HBox topRowContainer;
    @FXML private HBox bottomRowContainer;
    @FXML private StackPane deckContainer;
    @FXML private Label deckCountLabel;
    @FXML private VBox turnOrderContainer;
    @FXML private HBox offerTilesContainer;
    @FXML private BorderPane rootPane;
    @FXML private HBox boardTrackContainer;

    @FXML private FlowPane playersContainer;

    private JavaFXView view;
    private String myPlayerId;
    private GameState lastState;
    private boolean skipDialogShown = false;

    private static final double CARD_ASPECT = 1111.0 / 756.0;
    private static final double TILE_ASPECT = 932.0 / 582.0;

    private double cardW = 75;
    private double cardH = cardW * CARD_ASPECT;
    private double tileW = 78;
    private double tileH = tileW * TILE_ASPECT;
    private double playerPanelW = 210;

    private Stage primaryStage;

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    public void initialize() {
        applyBackgroundImage();

        topRowContainer.setAlignment(Pos.CENTER);
        bottomRowContainer.setAlignment(Pos.CENTER);
        offerTilesContainer.setAlignment(Pos.CENTER);

        /*
         * The card rows are inside ScrollPanes with fitToWidth=true.
         * Therefore they must be allowed to expand to the viewport width,
         * otherwise their content may stay visually offset inside the ScrollPane.
         */
        topRowContainer.setMaxWidth(Double.MAX_VALUE);
        bottomRowContainer.setMaxWidth(Double.MAX_VALUE);

        /*
         * The offer track and board track should remain compact.
         * They are centered by their parent containers.
         */
        offerTilesContainer.setMaxWidth(Region.USE_PREF_SIZE);

        if (boardTrackContainer != null) {
            boardTrackContainer.setAlignment(Pos.CENTER);
            boardTrackContainer.setMaxWidth(Region.USE_PREF_SIZE);
        }

        if (turnOrderContainer != null) {
            turnOrderContainer.setAlignment(Pos.CENTER);
            turnOrderContainer.setSpacing(0);
            turnOrderContainer.setPadding(Insets.EMPTY);
            turnOrderContainer.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        }

        rootPane.widthProperty().addListener((obs, oldW, newW) -> {
            updateResponsiveMetrics(newW.doubleValue());

            if (lastState != null) {
                updateBoard(lastState.getBoard(), lastState.getPlayers(), lastState.getPhase());
                updatePlayers(lastState.getPlayers());
            }
        });

        Platform.runLater(() -> {
            updateResponsiveMetrics(rootPane.getWidth());

            if (lastState != null) {
                updateBoard(lastState.getBoard(), lastState.getPlayers(), lastState.getPhase());
                updatePlayers(lastState.getPlayers());
            }
        });
    }

    private void applyBackgroundImage() {
        String path = "/images/Box_background.png";

        try (InputStream inputStream = getClass().getResourceAsStream(path)) {
            if (inputStream == null) {
                System.err.println("Background image not found: " + path);
                rootPane.setStyle("-fx-background-color: #1a4f12;");
                return;
            }

            Image image = new Image(inputStream);
            backgroundImageView.setImage(image);

            backgroundImageView.fitWidthProperty().bind(rootStack.widthProperty());
            backgroundImageView.fitHeightProperty().bind(rootStack.heightProperty());

            /*
             * Fill the whole window without bands or crop.
             * The image may be slightly stretched, but this is acceptable for a background.
             */
            backgroundImageView.setPreserveRatio(false);
            backgroundImageView.setSmooth(true);
            backgroundImageView.setCache(true);
        } catch (Exception e) {
            System.err.println("Failed to load background image: " + e.getMessage());
            rootPane.setStyle("-fx-background-color: #1a4f12;");
        }
    }

    private void updateResponsiveMetrics(double width) {
        if (width <= 0) {
            return;
        }

        cardW = Math.max(60, Math.min(82, width / 18.0));
        cardH = cardW * CARD_ASPECT;

        tileW = Math.max(58, Math.min(78, width / 20.0));
        tileH = tileW * TILE_ASPECT;

        playerPanelW = Math.max(190, Math.min(260, width / 6.0));

        topRowContainer.setMinHeight(cardH + 12);
        topRowContainer.setPrefHeight(cardH + 12);

        bottomRowContainer.setMinHeight(cardH + 12);
        bottomRowContainer.setPrefHeight(cardH + 12);

        offerTilesContainer.setMinHeight(tileH + 8);
        offerTilesContainer.setPrefHeight(tileH + 8);
    }

    public void setView(JavaFXView view) {
        this.view = view;
    }

    public void setMyPlayerId(String playerId) {
        this.myPlayerId = playerId;
    }

    // ENTRY POINT

    public void updateGameState(GameState state) {
        Platform.runLater(() -> {
            this.lastState = state;

            updateTopBar(state);
            updateBoard(state.getBoard(), state.getPlayers(), state.getPhase());
            updatePlayers(state.getPlayers());

            boolean isMyTurn = myPlayerId != null && myPlayerId.equals(state.getCurrentPlayerId());
            boolean isDrawPhase = state.getPhase() == GamePhase.RESOLVING_OFFERS;
            boolean shouldSkip = isMyTurn && isDrawPhase && state.getSkipAllowed();

            if (shouldSkip && !skipDialogShown) {
                skipDialogShown = true;
                showSkipDialog();
            } else if (!shouldSkip) {
                skipDialogShown = false;
            }
        });
    }

    // TOP BAR

    private void updateTopBar(GameState state) {
        eraLabel.setText("Era " + toRoman(state.getCurrentEra().ordinal() + 1));
        roundLabel.setText("Round " + state.getCurrentRound() + " / 10");
        phaseLabel.setText("Fase: " + formatPhase(state.getPhase().name()));
    }

    // BOARD

    private void updateBoard(BoardState board, List<PlayerState> players, GamePhase phase) {
        Map<String, String> nicknames = new HashMap<>();
        Map<String, String> colors = new HashMap<>();

        for (PlayerState p : players) {
            nicknames.put(p.getPlayerId(), p.getNickname());
            colors.put(p.getPlayerId(), resolveTotemColor(p.getTotemColor()));
        }

        updateCardRow(topRowContainer, board.getTopRow(), board.getTopBuildings(), phase, true);
        updateCardRow(bottomRowContainer, board.getBottomRow(), board.getBottomBuildings(), phase, false);
        updateOfferTiles(board.getOfferTiles(), nicknames, colors, phase, board);
        updateTurnOrder(board.getTurnOrderSlots(), nicknames, colors);
    }

    private void updateCardRow(HBox container,
                               List<CardState> characters,
                               List<CardState> buildings,
                               GamePhase phase,
                               boolean isTopRow) {
        container.getChildren().clear();

        if (characters == null || buildings == null) {
            return;
        }

        for (int i = 0; i < characters.size(); i++) {
            container.getChildren().add(buildCardNode(characters.get(i), phase, i, isTopRow));
        }

        for (int i = 0; i < buildings.size(); i++) {
            container.getChildren().add(buildCardNode(buildings.get(i), phase, i, isTopRow));
        }
    }

    private void updateOfferTiles(List<OfferTileState> tiles,
                                  Map<String, String> nicknames,
                                  Map<String, String> colors,
                                  GamePhase phase, BoardState board) {
        offerTilesContainer.getChildren().clear();
        offerTilesContainer.setAlignment(Pos.CENTER);
        offerTilesContainer.setMaxWidth(Region.USE_PREF_SIZE);

        if (tiles == null || tiles.isEmpty()) {
            return;
        }

        tiles.stream()
                .sorted(Comparator.comparingInt(OfferTileState::getPositionIndex))
                .forEach(tile -> offerTilesContainer.getChildren().add(
                        buildOfferTile(tile, nicknames, colors, phase, board)
                ));
    }

    private void updateTurnOrder(List<TurnOrderSlotState> slots,
                                 Map<String, String> nicknames,
                                 Map<String, String> colors) {
        turnOrderContainer.getChildren().clear();

        if (slots == null || slots.isEmpty()) {
            return;
        }

        List<TurnOrderSlotState> sortedSlots = slots.stream()
                .sorted(Comparator.comparingInt(TurnOrderSlotState::getPositionIndex))
                .toList();

        double width = Math.max(62, Math.min(78, tileW * 0.95));
        double height = width * TILE_ASPECT;

        turnOrderContainer.setAlignment(Pos.CENTER);
        turnOrderContainer.setSpacing(0);
        turnOrderContainer.setPadding(Insets.EMPTY);

        turnOrderContainer.setMinSize(width, height);
        turnOrderContainer.setPrefSize(width, height);
        turnOrderContainer.setMaxSize(width, height);

        turnOrderContainer.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-padding: 0;"
        );

        turnOrderContainer.getChildren().add(
                TurnOrderNodeFactory.createTurnOrderNode(
                        sortedSlots,
                        nicknames,
                        colors,
                        width,
                        height
                )
        );
    }

    // CARDS

    private VBox buildCardNode(CardState card, GamePhase phase, int boardIndex, boolean isTopRow) {
        VBox box = CardNodeFactory.createCardNode(card, cardW, cardH);

        boolean canTake = phase == GamePhase.RESOLVING_OFFERS
                && card.getCardKind() != CardKind.EVENT
                && myPlayerId != null;

        if (canTake) {
            boolean isBuilding = card.getCardKind() == CardKind.BUILDING;
            RowType row = isTopRow ? RowType.TOP : RowType.BOTTOM;
            SelectedSingleCard selected = new SelectedSingleCard(row, boardIndex, isBuilding);

            box.setStyle(box.getStyle() + "-fx-cursor: hand;");
            box.setOnMouseClicked(e -> {
                box.setOnMouseClicked(null);
                view.takeSingleCard(selected);
            });
            box.setOnMouseEntered(e -> box.setOpacity(0.75));
            box.setOnMouseExited(e -> box.setOpacity(1.0));
        }
        return box;
    }

    // EXTRA DRAW

    public void showExtraDrawDialog(GameState gameState) {
        List<CardState> allTopRow = gameState.getBoard().getTopRow();
        List<CardState> buildings = new ArrayList<>(gameState.getBoard().getTopBuildings());

        boolean hasSelectableCards = allTopRow.stream().anyMatch(c -> c.getCardKind() != CardKind.EVENT);
        if (!hasSelectableCards && buildings.isEmpty()) {
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Pescaggio Extra");

        VBox root = new VBox(12);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #3d1a0a;");

        Label title = new Label("Scegli una carta dalla fila superiore.");
        title.setStyle("-fx-text-fill: #f5f0e8; -fx-font-size: 14px; -fx-font-weight: bold;");

        HBox cardsRow = new HBox(8);
        cardsRow.setAlignment(Pos.CENTER);

        for (int i = 0; i < allTopRow.size(); i++) {
            CardState cardState = allTopRow.get(i);

            if (cardState.getCardKind() == CardKind.EVENT) {
                continue;
            }

            final int index = i;
            VBox card = CardNodeFactory.createCardNode(cardState, cardW, cardH);

            card.setOnMouseClicked(e -> {
                card.setOnMouseClicked(null);
                view.takeExtraCard(index, true);
                dialog.close();
            });

            card.setOnMouseEntered(e -> card.setOpacity(0.75));
            card.setOnMouseExited(e -> card.setOpacity(1.0));
            card.setStyle(card.getStyle() + "-fx-cursor: hand;");

            cardsRow.getChildren().add(card);
        }

        for (int i = 0; i < buildings.size(); i++) {
            final int index = i;
            VBox card = CardNodeFactory.createCardNode(buildings.get(i), cardW, cardH);

            card.setOnMouseClicked(e -> {
                card.setOnMouseClicked(null);
                view.takeExtraCard(index, false);
                dialog.close();
            });

            card.setOnMouseEntered(ev -> card.setOpacity(0.75));
            card.setOnMouseExited(ev -> card.setOpacity(1.0));
            card.setStyle(card.getStyle() + "-fx-cursor: hand;");

            cardsRow.getChildren().add(card);
        }

        root.getChildren().addAll(title, cardsRow);

        if (gameState.getSkipAllowed()) {
            javafx.scene.control.Button skipButton = new javafx.scene.control.Button("Salta turno");
            skipButton.setStyle(
                    "-fx-background-color: #5a2e10;" +
                            "-fx-text-fill: #f5f0e8;" +
                            "-fx-font-size: 12px;" +
                            "-fx-padding: 6 16 6 16;" +
                            "-fx-background-radius: 6;" +
                            "-fx-cursor: hand;"
            );

            skipButton.setOnAction(e -> {
                view.skipTurn();
                dialog.close();
            });

            root.getChildren().add(skipButton);
        }

        dialog.setScene(new javafx.scene.Scene(root));
        dialog.show();
    }

    // OFFER TILES

    private StackPane buildOfferTile(OfferTileState tile,
                                     Map<String, String> nicknames,
                                     Map<String, String> colors,
                                     GamePhase phase, BoardState currentBoard) {
        String occupied = tile.getOccupiedByPlayerId();

        String borderColor = occupied != null
                ? colors.getOrDefault(occupied, "#f5f0e8")
                : "#f5f0e840";

        String occupantName = occupied != null
                ? nicknames.getOrDefault(occupied, "?")
                : null;

        StackPane box = OfferTileNodeFactory.createOfferTileNode(
                tile,
                tileW,
                tileH,
                borderColor,
                occupantName
        );

        // evidenzia la tile del giocatore corrente in fase di pescaggio
        boolean isMyActiveTile = myPlayerId != null
                && myPlayerId.equals(occupied)
                && phase == GamePhase.RESOLVING_OFFERS
                && lastState != null
                && myPlayerId.equals(lastState.getCurrentPlayerId());

        if (isMyActiveTile) {
            String myColor = colors.getOrDefault(myPlayerId, "#f5f0e8");
            // crea un rettangolo overlay con bordo colorato
            javafx.scene.shape.Rectangle border = new javafx.scene.shape.Rectangle(tileW, tileH);
            border.setFill(javafx.scene.paint.Color.TRANSPARENT);
            border.setStroke(javafx.scene.paint.Color.web(myColor));
            border.setStrokeWidth(3);
            border.setArcWidth(6);
            border.setArcHeight(6);
            border.setMouseTransparent(true); // non interferisce con i click
            box.getChildren().add(border);
        }

        boolean alreadyPlaced = currentBoard.getOfferTiles().stream()
                .anyMatch(t -> myPlayerId != null && myPlayerId.equals(t.getOccupiedByPlayerId()));

        boolean isMyPlacingTurn = lastState != null
                && myPlayerId != null
                && myPlayerId.equals(lastState.getCurrentPlayerId());

        boolean canPlace = isMyPlacingTurn
                && phase == GamePhase.PLACING_TOTEMS
                && tile.getOccupiedByPlayerId() == null
                && !alreadyPlaced;

        if (canPlace) {
            box.setStyle(box.getStyle() + "-fx-cursor: hand;");
            box.setOnMouseClicked(e -> view.placeTotem(tile.getTileId()));
            box.setOnMouseEntered(e -> box.setOpacity(0.75));
            box.setOnMouseExited(e -> box.setOpacity(1.0));
        }
        return box;
    }

    private void showSkipDialog() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION
        );

        alert.setTitle("Nessuna carta disponibile.");
        alert.setHeaderText(null);
        alert.setContentText("Non ci sono carte pescabili. Vuoi saltare il turno?");

        javafx.scene.control.ButtonType skipButton = new javafx.scene.control.ButtonType("Salta turno");
        javafx.scene.control.ButtonType cancelButton = new javafx.scene.control.ButtonType(
                "Annulla",
                javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE
        );

        alert.getButtonTypes().setAll(skipButton, cancelButton);

        alert.showAndWait().ifPresent(result -> {
            if (result == skipButton) {
                view.skipTurn();
            }
        });
    }

    // EVENTS
    public void showEventsResolvedDialog(List<EventResolvedPayload> events, List<PlayerState> players) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.NONE);
        dialog.setTitle("Eventi del round");

        VBox root = new VBox(16);
        root.setAlignment(Pos.TOP_LEFT);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #2a1205;");

        Map<String, String> nicknames = new HashMap<>();
        for (PlayerState p : players) nicknames.put(p.getPlayerId(), p.getNickname());

        Map<String, int[]> totalDeltas = new HashMap<>();
        for (PlayerState p : players) totalDeltas.put(p.getPlayerId(), new int[]{0, 0});

        for (EventResolvedPayload event : events) {
            Label eventLabel = new Label("◆ " + event.eventCardId());
            eventLabel.setStyle("-fx-text-fill: #f5d78e; -fx-font-size: 14px; -fx-font-weight: bold;");
            root.getChildren().add(eventLabel);

            for (PlayerState p : players) {
                PlayerDelta delta = event.playerDeltas().stream()
                        .filter(d -> d.playerId().equals(p.getPlayerId()))
                        .findFirst().orElse(null);

                String nick = nicknames.getOrDefault(p.getPlayerId(), p.getPlayerId());
                String foodStr = delta == null || delta.foodDelta() == 0 ? ""
                        : (delta.foodDelta() > 0 ? "+" : "") + delta.foodDelta() + "🍖";
                String ppStr = delta == null || delta.prestigeDelta() == 0 ? ""
                        : (delta.prestigeDelta() > 0 ? "+" : "") + delta.prestigeDelta() + "⭐";
                String changes = foodStr.isEmpty() && ppStr.isEmpty()
                        ? "nessuna variazione"
                        : foodStr + (ppStr.isEmpty() ? "" : " " + ppStr);

                Label deltaLabel = new Label("  " + nick + ": " + changes);
                deltaLabel.setStyle("-fx-text-fill: #f5f0e8; -fx-font-size: 12px;");
                root.getChildren().add(deltaLabel);

                if (delta != null) {
                    int[] acc = totalDeltas.get(p.getPlayerId());
                    if (acc != null) { acc[0] += delta.foodDelta(); acc[1] += delta.prestigeDelta(); }
                }
            }

            Region sep = new Region();
            sep.setPrefHeight(1);
            sep.setStyle("-fx-background-color: rgba(255,255,255,0.1);");
            root.getChildren().add(sep);
        }

        if (events.size() > 1) {
            Label summaryLabel = new Label("Riepilogo round:");
            summaryLabel.setStyle("-fx-text-fill: #f5d78e; -fx-font-size: 14px; -fx-font-weight: bold;");
            root.getChildren().add(summaryLabel);

            for (PlayerState p : players) {
                int[] acc = totalDeltas.get(p.getPlayerId());
                String nick = nicknames.getOrDefault(p.getPlayerId(), p.getPlayerId());
                String foodStr = acc[0] == 0 ? "" : (acc[0] > 0 ? "+" : "") + acc[0] + "🍖";
                String ppStr = acc[1] == 0 ? "" : (acc[1] > 0 ? "+" : "") + acc[1] + "⭐";
                String changes = foodStr.isEmpty() && ppStr.isEmpty()
                        ? "nessuna variazione"
                        : foodStr + (ppStr.isEmpty() ? "" : " " + ppStr);
                Label l = new Label("  " + nick + ": " + changes);
                l.setStyle("-fx-text-fill: #f5f0e8; -fx-font-size: 12px;");
                root.getChildren().add(l);
            }
        }

        javafx.scene.control.Button proceedButton = new javafx.scene.control.Button("Continua");
        proceedButton.setStyle(
                "-fx-background-color: #5a2e10; -fx-text-fill: #f5f0e8;" +
                        "-fx-font-size: 12px; -fx-padding: 6 16 6 16;" +
                        "-fx-background-radius: 6; -fx-cursor: hand;"
        );
        root.getChildren().add(proceedButton);

        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #2a1205; -fx-background-color: #2a1205;");

        dialog.setScene(new javafx.scene.Scene(scroll, 420, 500));

        if (primaryStage != null) {
            dialog.setX(primaryStage.getX() + primaryStage.getWidth() - 440);
            dialog.setY(primaryStage.getY() + 40);
        }

        dialog.show();

        proceedButton.setOnAction(e -> dialog.close());
    }


    // PLAYER BOARDS

    private void updatePlayers(List<PlayerState> players) {
        playersContainer.getChildren().clear();

        for (PlayerState player : players) {
            boolean isMe = player.getPlayerId().equals(myPlayerId);
            playersContainer.getChildren().add(buildPlayerPanel(player, isMe, playerPanelW));
        }
    }

    private VBox buildPlayerPanel(PlayerState player, boolean isMe, double width) {
        String totemHex = resolveTotemColor(player.getTotemColor());

        VBox card = new VBox(5);
        card.setPrefWidth(width);
        card.setPadding(new Insets(8, 10, 8, 10));
        card.setStyle(
                "-fx-background-color: rgba(18,6,3,0.82);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: " + totemHex + ";" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 2;"
        );
        HBox nameRow = new HBox();
        nameRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(player.getNickname());
        nameLabel.setStyle("-fx-text-fill: #f5f0e8; -fx-font-size: 13px; -fx-font-weight: bold;");
        nameRow.getChildren().add(nameLabel);

        if (isMe) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label meLabel = new Label("TU");
            meLabel.setStyle("-fx-text-fill: rgba(245,240,232,0.45); -fx-font-size: 10px;");

            nameRow.getChildren().addAll(spacer, meLabel);
        }

        HBox resourcesRow = new HBox(10);
        resourcesRow.setAlignment(Pos.CENTER_LEFT);
        resourcesRow.getChildren().addAll(
                buildResource("🍖", String.valueOf(player.getFood())),
                buildResource("⭐", player.getPrestigePoints() + " PP")
        );

        Region div1 = new Region();
        div1.setPrefHeight(1);
        div1.setStyle("-fx-background-color: rgba(255,255,255,0.1);");

        GridPane statsGrid = buildStatsGrid(player);

        Region div2 = new Region();
        div2.setPrefHeight(1);
        div2.setStyle("-fx-background-color: rgba(255,255,255,0.08);");

        HBox totalRow = new HBox(5);
        totalRow.setAlignment(Pos.CENTER_LEFT);

        Label totalLabel = new Label("tot. personaggi:");
        totalLabel.setStyle("-fx-text-fill: rgba(245,240,232,0.55); -fx-font-size: 11px;");

        Label totalVal = new Label(String.valueOf(player.getCharacters().size()));
        totalVal.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");

        totalRow.getChildren().addAll(totalLabel, totalVal);

        Region div3 = new Region();
        div3.setPrefHeight(1);
        div3.setStyle("-fx-background-color: rgba(255,255,255,0.08);");

        VBox buildingsSection = buildBuildingsSection(player.getBuildings());

        card.getChildren().addAll(
                nameRow,
                resourcesRow,
                div1,
                statsGrid,
                div2,
                totalRow,
                div3,
                buildingsSection
        );

        return card;
    }

    private HBox buildResource(String icon, String value) {
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 12px;");

        Label valLabel = new Label(value);
        valLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

        row.getChildren().addAll(iconLabel, valLabel);
        return row;
    }

    private GridPane buildStatsGrid(PlayerState player) {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(4);

        Map<CharacterType, Long> counts = new HashMap<>();
        int totalStars = 0;
        int totalDiscount = 0;
        Set<Object> inventionIcons = new HashSet<>();

        for (CardState card : player.getCharacters()) {
            if (card instanceof CharacterCardState c) {
                counts.merge(c.getCharacterType(), 1L, Long::sum);

                if (c.getCharacterType() == CharacterType.SHAMAN && c.getStars() != null) {
                    totalStars += c.getStars();
                }

                if (c.getCharacterType() == CharacterType.BUILDER && c.getDiscount() != null) {
                    totalDiscount += c.getDiscount();
                }

                if (c.getCharacterType() == CharacterType.INVENTOR && c.getInventionIcon() != null) {
                    inventionIcons.add(c.getInventionIcon());
                }
            }
        }

        int finalTotalStars = totalStars;
        int finalTotalDiscount = totalDiscount;
        int differentIcons = inventionIcons.size();

        Object[][] rows = {
                {"🎨", "Artists", counts.getOrDefault(CharacterType.ARTIST, 0L), null, null},
                {"🔧", "Builders", counts.getOrDefault(CharacterType.BUILDER, 0L), "-" + finalTotalDiscount + "🍖", null},
                {"🏹", "Hunters", counts.getOrDefault(CharacterType.HUNTER, 0L), null, null},
                {"💡", "Inventors", counts.getOrDefault(CharacterType.INVENTOR, 0L), null, "⬡" + differentIcons},
                {"🍓", "Gatherers", counts.getOrDefault(CharacterType.GATHERER, 0L), null, null},
                {"🔮", "Shamans", counts.getOrDefault(CharacterType.SHAMAN, 0L), "★" + finalTotalStars, null},
        };

        for (int i = 0; i < rows.length; i++) {
            int col = (i % 2) * 3;
            int row = i / 2;

            Label icon = new Label((String) rows[i][0]);
            icon.setStyle("-fx-font-size: 14px;");

            Label val = new Label(String.valueOf(rows[i][2]));
            val.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");

            grid.add(icon, col, row);
            grid.add(val, col + 1, row);

            String extra1 = (String) rows[i][3];
            String extra2 = (String) rows[i][4];
            String extraText = extra1 != null ? extra1 : (extra2 != null ? extra2 : "");

            if (!extraText.isEmpty()) {
                Label extraLabel = new Label(extraText);
                extraLabel.setStyle("-fx-text-fill: rgba(245,240,232,0.6); -fx-font-size: 10px;");
                grid.add(extraLabel, col + 2, row);
            }
        }

        return grid;
    }

    private VBox buildBuildingsSection(List<CardState> buildings) {
        VBox section = new VBox(4);

        Label label = new Label("BUILDINGS");
        label.setStyle("-fx-text-fill: rgba(245,240,232,0.35); -fx-font-size: 10px;");

        HBox slotsRow = new HBox(4);
        slotsRow.setAlignment(Pos.CENTER_LEFT);
        slotsRow.setMinHeight(36);

        for (CardState b : buildings) {
            VBox slot = CardNodeFactory.createCardNode(b, 26, 36);
            slotsRow.getChildren().add(slot);
        }

        int emptySlots = Math.max(2, 4 - buildings.size());

        for (int i = 0; i < emptySlots; i++) {
            VBox slot = new VBox();
            slot.setPrefSize(26, 36);
            slot.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.04);" +
                            "-fx-background-radius: 4;" +
                            "-fx-border-color: rgba(255,255,255,0.15);" +
                            "-fx-border-radius: 4;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-style: dashed;"
            );

            slotsRow.getChildren().add(slot);
        }

        section.getChildren().addAll(label, slotsRow);
        return section;
    }

    // HELPERS

    private String resolveTotemColor(String totemColor) {
        if (totemColor == null) {
            return "#f5f0e8";
        }

        if (totemColor.startsWith("#")) {
            return totemColor;
        }

        try {
            return TotemColors.valueOf(totemColor.toUpperCase()).getColor();
        } catch (IllegalArgumentException e) {
            return "#f5f0e8";
        }
    }

    private String toRoman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> String.valueOf(n);
        };
    }

    private String formatPhase(String phase) {
        return phase.replace("_", " ").toLowerCase();
    }
}
