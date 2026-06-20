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
import it.polimi.ingsw.am23.view.gui.components.*;
import javafx.application.Platform;
import javafx.stage.Window;
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
    @FXML private StackPane summaryButton;

    private JavaFXView view;
    private String myPlayerId;
    private GameState lastState;
    private boolean skipDialogShown = false;

    private static final double CARD_ASPECT = 1111.0 / 756.0;
    private static final double TILE_ASPECT = 932.0 / 582.0;

    private double cardW = 95;
    private double cardH = cardW * CARD_ASPECT;
    private double tileW = 98;
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

        if (summaryButton != null) {
            summaryButton.setOnMouseClicked(e -> showSummaryDialog());
            summaryButton.setOnMouseEntered(e -> summaryButton.setOpacity(0.72));
            summaryButton.setOnMouseExited(e -> summaryButton.setOpacity(1.0));
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

        cardW = Math.max(80, Math.min(110, width / 13.0));
        cardH = cardW * CARD_ASPECT;

        tileW = Math.max(80, Math.min(110, width / 14.0));
        tileH = tileW * TILE_ASPECT;

        playerPanelW = Math.max(190, Math.min(260, width / 6.0));

        topRowContainer.setMinHeight(cardH + 8);
        topRowContainer.setPrefHeight(cardH + 8);

        bottomRowContainer.setMinHeight(cardH + 8);
        bottomRowContainer.setPrefHeight(cardH + 8);

        offerTilesContainer.setMinHeight(tileH + 4);
        offerTilesContainer.setPrefHeight(tileH + 4);
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
            updateDeckBack(state);
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

    //DECK BACK

    private void updateDeckBack(GameState state) {
        deckContainer.getChildren().clear();

        int eraNumber = state.getCurrentEra().ordinal() + 1;

        double deckW = cardW;
        double deckH = cardH;

        deckContainer.setMinSize(deckW, deckH);
        deckContainer.setPrefSize(deckW, deckH);
        deckContainer.setMaxSize(deckW, deckH);

        deckContainer.setStyle(
                "-fx-background-color: rgba(107,58,31,0.40);" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: rgba(245,240,232,0.70);" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 1;"
        );

        ImageView back = new ImageView(CardImageResolver.loadCharacterBackImage(eraNumber));
        back.setFitWidth(deckW);
        back.setFitHeight(deckH);
        back.setPreserveRatio(false);
        back.setSmooth(true);
        back.setCache(true);

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(deckW, deckH);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        back.setClip(clip);

        deckContainer.getChildren().add(back);
    }

    // TOP BAR

    private void updateTopBar(GameState state) {
        eraLabel.setText("Era " + toRoman(state.getCurrentEra().ordinal() + 1));
        roundLabel.setText("Round " + state.getCurrentRound() + " / 10");
        phaseLabel.setText("Phase: " + formatPhase(state.getPhase().name()));
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

        double width = Math.max(75, Math.min(100, tileW * 0.95));
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
        dialog.setTitle("Extra Draw");

        VBox root = new VBox(12);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #3d1a0a;");

        Label title = new Label("Choose a card from the top row.");
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
            javafx.scene.control.Button skipButton = new javafx.scene.control.Button("Skip turn");
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

        alert.setTitle("No cards available.");
        alert.setHeaderText(null);
        alert.setContentText("There are no cards to draw. Do you want to skip your turn?");

        javafx.scene.control.ButtonType skipButton = new javafx.scene.control.ButtonType("Skip turn");
        javafx.scene.control.ButtonType cancelButton = new javafx.scene.control.ButtonType(
                "Cancel",
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

        Window owner = rootPane != null && rootPane.getScene() != null
                ? rootPane.getScene().getWindow()
                : null;

        if (owner != null) {
            dialog.initOwner(owner);
            dialog.initModality(Modality.WINDOW_MODAL);
        } else {
            dialog.initModality(Modality.NONE);
        }

        dialog.setTitle("Round events");

        VBox root = new VBox(16);
        root.setAlignment(Pos.TOP_LEFT);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #2a1205;");

        Map<String, String> nicknames = new HashMap<>();
        for (PlayerState p : players) {
            nicknames.put(p.getPlayerId(), p.getNickname());
        }

        Map<String, int[]> totalDeltas = new HashMap<>();
        for (PlayerState p : players) {
            totalDeltas.put(p.getPlayerId(), new int[]{0, 0});
        }

        for (EventResolvedPayload event : events) {
            Label eventLabel = new Label("◆ " + formatEventTitle(event));
            eventLabel.setStyle("-fx-text-fill: #f5d78e; -fx-font-size: 14px; -fx-font-weight: bold;");
            root.getChildren().add(eventLabel);

            for (PlayerState p : players) {
                PlayerDelta delta = event.playerDeltas().stream()
                        .filter(d -> d.playerId().equals(p.getPlayerId()))
                        .findFirst()
                        .orElse(null);

                String nick = nicknames.getOrDefault(p.getPlayerId(), p.getPlayerId());

                root.getChildren().add(buildEventDeltaRow(nick, delta));

                if (delta != null) {
                    int[] acc = totalDeltas.get(p.getPlayerId());
                    if (acc != null) {
                        acc[0] += delta.foodDelta();
                        acc[1] += delta.prestigeDelta();
                    }
                }
            }

            Region separator = new Region();
            separator.setPrefHeight(1);
            separator.setStyle("-fx-background-color: rgba(255,255,255,0.1);");
            root.getChildren().add(separator);
        }

        if (events.size() > 1) {
            Label summaryLabel = new Label("Round summary:");
            summaryLabel.setStyle("-fx-text-fill: #f5d78e; -fx-font-size: 14px; -fx-font-weight: bold;");
            root.getChildren().add(summaryLabel);

            for (PlayerState p : players) {
                int[] acc = totalDeltas.get(p.getPlayerId());
                String nick = nicknames.getOrDefault(p.getPlayerId(), p.getPlayerId());

                root.getChildren().add(buildEventSummaryRow(nick, acc[0], acc[1]));
            }
        }

        javafx.scene.control.Button proceedButton = new javafx.scene.control.Button("Continue");
        proceedButton.setStyle(
                "-fx-background-color: #5a2e10;" +
                        "-fx-text-fill: #f5f0e8;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 6 16 6 16;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
        );
        proceedButton.setOnAction(e -> dialog.close());
        root.getChildren().add(proceedButton);

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2a1205; -fx-background-color: #2a1205;");

        dialog.setScene(new javafx.scene.Scene(scrollPane, 420, 500));
        dialog.setOnShown(e -> centerDialogOnOwner(dialog, owner));
        dialog.show();
    }

    // SUMMARY DIALOG

    private void showSummaryDialog() {
        Stage dialog = new Stage();

        Window owner = rootPane != null && rootPane.getScene() != null
                ? rootPane.getScene().getWindow()
                : null;

        if (owner != null) {
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
        }

        dialog.setTitle("Summary Card");

        Image summary1 = CardImageResolver.loadSummaryImage(1);
        Image summary2 = CardImageResolver.loadSummaryImage(2);

        double imgW = 190;
        double imgH = 270;

        VBox root = new VBox(16);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #1e0a04;");

        HBox imagesRow = new HBox(16);
        imagesRow.setAlignment(Pos.CENTER);
        imagesRow.getChildren().addAll(
                buildSummaryCardFace(summary1, imgW, imgH),
                buildSummaryCardFace(summary2, imgW, imgH)
        );

        javafx.scene.control.Button closeButton = new javafx.scene.control.Button("Close");
        closeButton.setStyle(
                "-fx-background-color: #5a2e10;" +
                        "-fx-text-fill: #f5f0e8;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 6 20 6 20;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
        );
        closeButton.setOnAction(e -> dialog.close());

        root.getChildren().addAll(imagesRow, closeButton);

        dialog.setScene(new javafx.scene.Scene(root, imgW * 2 + 70, imgH + 100));
        dialog.setResizable(true);
        dialog.setOnShown(e -> centerDialogOnOwner(dialog, owner));
        dialog.show();
    }

    private VBox buildSummaryCardFace(Image image, double imgW, double imgH) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.TOP_CENTER);

        if (image != null) {
            ImageView iv = new ImageView(image);
            iv.setFitWidth(imgW);
            iv.setFitHeight(imgH);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);

            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(imgW, imgH);
            clip.setArcWidth(10);
            clip.setArcHeight(10);
            iv.setClip(clip);

            StackPane imagePane = new StackPane(iv);
            imagePane.setMaxSize(imgW, imgH);
            imagePane.setStyle(
                    "-fx-border-color: rgba(245,240,232,0.25);" +
                            "-fx-border-radius: 8;" +
                            "-fx-border-width: 1;"
            );

            box.getChildren().addAll(imagePane);
        } else {
            StackPane placeholder = new StackPane();
            placeholder.setPrefSize(imgW, imgH);
            placeholder.setStyle(
                    "-fx-background-color: rgba(107,58,31,0.40);" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-color: rgba(245,240,232,0.25);" +
                            "-fx-border-radius: 8;" +
                            "-fx-border-width: 1;"
            );
            Label missing = new Label("Image not found\n");
            missing.setStyle("-fx-text-fill: rgba(245,240,232,0.55); -fx-font-size: 12px; -fx-text-alignment: center;");
            placeholder.getChildren().add(missing);
            box.getChildren().addAll(placeholder);
        }

        return box;
    }


    //helper
    private String formatEventTitle(EventResolvedPayload event) {
        return eventTypeName(event.eventCardId())
                + " - Era " + toRoman(event.era().ordinal() + 1)
                + " [" + event.eventCardId() + "]";
    }
    private HBox buildEventDeltaRow(String nickname, PlayerDelta delta) {
        int foodDelta = delta != null ? delta.foodDelta() : 0;
        int prestigeDelta = delta != null ? delta.prestigeDelta() : 0;

        return buildEventChangeRow(nickname, foodDelta, prestigeDelta);
    }

    private HBox buildEventSummaryRow(String nickname, int foodDelta, int prestigeDelta) {
        return buildEventChangeRow(nickname, foodDelta, prestigeDelta);
    }

    private HBox buildEventChangeRow(String nickname, int foodDelta, int prestigeDelta) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 0 0 0 10;");

        Label nameLabel = new Label(nickname + ":");
        nameLabel.setMinWidth(90);
        nameLabel.setStyle("-fx-text-fill: #f5f0e8; -fx-font-size: 12px;");

        row.getChildren().add(nameLabel);

        boolean hasChanges = false;

        if (foodDelta != 0) {
            row.getChildren().add(
                    IconNodeFactory.createSmallIconWithText(
                            IconNodeFactory.FOOD_ICON,
                            formatSigned(foodDelta),
                            13
                    )
            );
            hasChanges = true;
        }

        if (prestigeDelta != 0) {
            row.getChildren().add(
                    IconNodeFactory.createSmallIconWithText(
                            IconNodeFactory.PRESTIGE_ICON,
                            formatSigned(prestigeDelta),
                            13
                    )
            );
            hasChanges = true;
        }

        if (!hasChanges) {
            Label noChange = new Label("no change");
            noChange.setStyle("-fx-text-fill: rgba(245,240,232,0.65); -fx-font-size: 12px;");
            row.getChildren().add(noChange);
        }

        return row;
    }

    private String formatSigned(int value) {
        return value > 0 ? "+" + value : String.valueOf(value);
    }

    private String eventTypeName(String eventCardId) {
        if (eventCardId == null) {
            return "Event";
        }

        if (eventCardId.startsWith("ECP")) {
            return "Cave Painting";
        }

        if (eventCardId.startsWith("EHU")) {
            return "Hunt";
        }

        if (eventCardId.startsWith("ESH")) {
            return "Shamanic";
        }

        if (eventCardId.startsWith("ESU")) {
            return "Sustenance";
        }

        return "Event";
    }

    private void centerDialogOnOwner(Stage dialog, Window owner) {
        if (owner == null) {
            dialog.centerOnScreen();
            return;
        }

        double x = owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2.0;
        double y = owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2.0;

        dialog.setX(x);
        dialog.setY(y);
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

        VBox card = new VBox(4);
        card.setPrefWidth(width);
        card.setPadding(new Insets(6, 10, 6, 10));
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

            Label meLabel = new Label("YOU");
            meLabel.setStyle("-fx-text-fill: rgba(245,240,232,0.75); -fx-font-size: 12px; -fx-font-weight: bold;");

            nameRow.getChildren().addAll(spacer, meLabel);
        }

        HBox resourcesRow = new HBox(10);
        resourcesRow.setAlignment(Pos.CENTER_LEFT);

        Region resSpacer = new Region();
        HBox.setHgrow(resSpacer, Priority.ALWAYS);

        Label totLabel = new Label("TOT: " + player.getCharacters().size());
        totLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");

        resourcesRow.getChildren().addAll(
                buildResource(IconNodeFactory.FOOD_ICON, String.valueOf(player.getFood())),
                buildResource(IconNodeFactory.PRESTIGE_ICON, String.valueOf(player.getPrestigePoints())),
                totLabel
        );

        Region div1 = new Region();
        div1.setPrefHeight(1);
        div1.setStyle("-fx-background-color: rgba(255,255,255,0.1);");

        GridPane statsGrid = buildStatsGrid(player);

        Region div2 = new Region();
        div2.setPrefHeight(1);
        div2.setStyle("-fx-background-color: rgba(255,255,255,0.08);");

        VBox buildingsSection = buildBuildingsSection(player.getBuildings());

        card.getChildren().addAll(
                nameRow,
                resourcesRow,
                div1,
                statsGrid,
                div2,
                buildingsSection
        );

        return card;
    }

    private HBox buildResource(String iconFileName, String value) {
        return IconNodeFactory.createIconWithText(iconFileName, value, 15);
    }

    private GridPane buildStatsGrid(PlayerState player) {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);

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
                {CharacterType.ARTIST, counts.getOrDefault(CharacterType.ARTIST, 0L), "   "},
                {CharacterType.BUILDER, counts.getOrDefault(CharacterType.BUILDER, 0L), "  - " + finalTotalDiscount + " 🍖"},
                {CharacterType.HUNTER, counts.getOrDefault(CharacterType.HUNTER, 0L), "   "},
                {CharacterType.INVENTOR, counts.getOrDefault(CharacterType.INVENTOR, 0L), "  ⬡ " + differentIcons},
                {CharacterType.GATHERER, counts.getOrDefault(CharacterType.GATHERER, 0L), "   "},
                {CharacterType.SHAMAN, counts.getOrDefault(CharacterType.SHAMAN, 0L), "  ★ " + finalTotalStars},
        };

        for (int i = 0; i < rows.length; i++) {
            int col = (i % 2) * 3;
            int row = i / 2;

            CharacterType type = (CharacterType) rows[i][0];

            ImageView icon = IconNodeFactory.createCharacterTypeIcon(type, 15);

            Label val = new Label(String.valueOf(rows[i][1]));
            val.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");

            grid.add(icon, col, row);
            grid.add(val, col + 1, row);

            String extraText = (String) rows[i][2];

            if (extraText != null && !extraText.isEmpty()) {
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

        int emptySlots = Math.max(2, 3 - buildings.size());

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
