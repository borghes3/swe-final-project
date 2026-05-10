package it.polimi.ingsw.am23.view.gui.controllers;

import it.polimi.ingsw.am23.model.draw.SelectedSingleCard;
import it.polimi.ingsw.am23.model.enums.*;
import it.polimi.ingsw.am23.model.state.*;
import it.polimi.ingsw.am23.view.gui.JavaFXView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;

public class GameScreenController {
    @FXML private Label eraLabel;
    @FXML private Label roundLabel;
    @FXML private Label phaseLabel;

    @FXML private HBox topRowContainer;
    @FXML private HBox bottomRowContainer;
    @FXML private StackPane deckContainer;
    @FXML private Label deckCountLabel;
    @FXML private VBox turnOrderContainer;
    @FXML private HBox offerTilesContainer;

    @FXML private FlowPane playersContainer;

    private JavaFXView view;
    private String myPlayerId;
    private GameState lastState;
    private boolean skipDialogShown = false;

    private double cardW = 75;
    private double cardH = 110;
    private double tileW = 78;
    private double tileH = 100;
    private double playerPanelW = 210;

    @FXML
    public void initialize(){
        playersContainer.widthProperty().addListener((obs, oldW, newW) -> {
            double w = newW.doubleValue();
            cardW = Math.max(65, Math.min(95, w/16.0));
            cardH = cardW * 1.45;
            tileW = Math.max(70, Math.min(100, w/14.0));
            tileH = tileW * 1.25;
            playerPanelW = Math.max(190, Math.min(260, w/6.0));

            if(lastState != null){
                updateBoard(lastState.getBoard(), lastState.getPlayers(), lastState.getPhase());
                updatePlayers(lastState.getPlayers());
            }
        });
    }

    public void setView(JavaFXView view){
        this.view = view;
    }

    public void setMyPlayerId(String playerId){
        this.myPlayerId = playerId;
    }


    // ENTRY POINT

    public void updateGameState(GameState state){
        Platform.runLater(() -> {
            this.lastState = state;
            updateTopBar(state);
            updateBoard(state.getBoard(), state.getPlayers(), state.getPhase());
            updatePlayers(state.getPlayers());

            boolean isMyTurn = myPlayerId != null && myPlayerId.equals(state.getCurrentPlayerId());
            boolean isDrawPhase = state.getPhase() == GamePhase.RESOLVING_OFFERS;
            boolean shouldSkip = isMyTurn && isDrawPhase && state.getSkipAllowed();

            if(shouldSkip && !skipDialogShown){
                skipDialogShown = true;
                showSkipDialog();
            } else if(!shouldSkip){
                skipDialogShown = false;
            }
        });
    }

    // TOP BAR

    private void updateTopBar(GameState state){
        eraLabel.setText("Era " + toRoman(state.getCurrentEra().ordinal()+1));
        roundLabel.setText("Round " + state.getCurrentRound() + " / 10");
        phaseLabel.setText("Fase: " + formatPhase(state.getPhase().name()));

    }

    // BOARD
    private void updateBoard(BoardState board, List<PlayerState> players, GamePhase phase){
        Map<String, String> nicknames = new HashMap<>();
        Map<String, String> colors = new HashMap<>();

        for(PlayerState p : players){
            nicknames.put(p.getPlayerId(), p.getNickname());
            colors.put(p.getPlayerId(), resolveTotemColor(p.getTotemColor()));
        }

        updateCardRow(topRowContainer, board.getTopRow(), board.getTopBuildings(), phase, true);
        updateCardRow(bottomRowContainer, board.getBottomRow(), board.getBottomBuildings(), phase, false);
        updateOfferTiles(board.getOfferTiles(), nicknames, colors, phase);
        updateTurnOrder(board.getTurnOrderSlots(), nicknames, colors);
    }

    private void updateCardRow(HBox container, List<CardState> characters, List<CardState> buildings, GamePhase phase, boolean isTopRow){
        container.getChildren().clear();
        if(characters == null || buildings == null)
            return;

        for(int i=0; i<characters.size(); i++){
            container.getChildren().add(buildCardPlaceholder(characters.get(i), phase, i, isTopRow));
        }
        for(int i=0; i<buildings.size(); i++){
            container.getChildren().add(buildBuildingCardPlaceholder(buildings.get(i), phase, i, isTopRow));
        }
    }

    private void updateOfferTiles(List<OfferTileState> tiles, Map<String, String> nicknames, Map<String, String> colors, GamePhase phase){
        offerTilesContainer.getChildren().clear();
        tiles.stream()
                .sorted(Comparator.comparingInt(OfferTileState::getPositionIndex))
                .forEach(tile -> offerTilesContainer.getChildren().add(buildOfferTile(tile, nicknames, colors, phase)));
    }

    private void updateTurnOrder(List<TurnOrderSlotState> slots, Map<String, String> nicknames, Map<String, String> colors){
        turnOrderContainer.getChildren().clear();
        slots.stream()
                .sorted(Comparator.comparingInt(TurnOrderSlotState::getPositionIndex))
                .forEach(slot -> turnOrderContainer.getChildren().add(buildTurnOrderSlot(slot, nicknames, colors)));

    }

    // CARTE PERSONAGGIO - Placeholder colorati per tipo

    private VBox buildCardPlaceholder(CardState card, GamePhase phase, int boardIndex, boolean isTopRow){
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setPrefSize(cardW, cardH);
        box.setMinSize(cardW, cardH);
        box.setMaxSize(cardW, cardH);
        box.setStyle("-fx-background-color: " + cardColor(card) + ";" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: rgba(255,255,255,0.2);" +
                "-fx-border-radius: 6;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 4;");

        Label idLabel = new Label(card.getCardId());
        idLabel.setStyle("-fx-text-fill: white; -fx-font-size: 8px;");
        idLabel.setWrapText(true);

        Label typeLabel = new Label(cardTypeName(card));
        typeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 9px; -fx-font-weight: bold;");

        box.getChildren().addAll(typeLabel, idLabel);

        if(card.getCardKind() == CardKind.BUILDING){
            BuildingCardState b = (BuildingCardState) card;
            Label foodLabel = new Label("🍖 " + b.getFoodCost());
            foodLabel.setStyle("-fx-text-fill: white; -fx-font-size: 9px; -fx-font-weight: bold;");
            box.getChildren().add(foodLabel);
        }

        boolean canTake = phase == GamePhase.RESOLVING_OFFERS && card.getCardKind() != CardKind.EVENT && myPlayerId != null;

        if(canTake){
            boolean isBuilging = card.getCardKind() == CardKind.BUILDING;
            RowType row = isTopRow ? RowType.TOP : RowType.BOTTOM;
            SelectedSingleCard selected = new SelectedSingleCard(row, boardIndex, isBuilging);

            box.setStyle(box.getStyle() + "-fx-cursor: hand;");
            box.setOnMouseClicked(e -> {box.setOnMouseClicked(null); view.takeSingleCard(selected);});
            box.setOnMouseEntered(e -> box.setOpacity(0.75));
            box.setOnMouseExited(e -> box.setOpacity(1.0));

        }

        return box;
    }

    private VBox buildBuildingCardPlaceholder(CardState card, GamePhase phase, int boardIndex, boolean isTopRow){
        VBox box = buildCardPlaceholder(card, phase, boardIndex, isTopRow);
        box.setStyle(box.getStyle() + "-fx-border-color: #f1c400; -fx-border-width: 2;");
        return box;
    }

    // EXTRA DRAW
    public void showExtraDrawDialog(GameState gameState){
        List<CardState> allTopRow = gameState.getBoard().getTopRow();
        List<CardState> buildings = new ArrayList<>(gameState.getBoard().getTopBuildings());

        boolean hasSelectableCards = allTopRow.stream().anyMatch(c -> c.getCardKind() != CardKind.EVENT);
        if(!hasSelectableCards && buildings.isEmpty())
            return;

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

        for(int i=0; i<allTopRow.size(); i++){
            CardState cardState = allTopRow.get(i);
            if(cardState.getCardKind() == CardKind.EVENT) continue;
            final int index = i;
            VBox card = buildCardPlaceholder(allTopRow.get(i), GamePhase.RESOLVING_OFFERS, i, true);
            card.setOnMouseClicked(e -> {
                card.setOnMouseClicked(null);
                view.takeExtraCard(index, true);
                dialog.close();
            });

            card.setOnMouseEntered(e -> card.setOpacity(0.75));
            card.setOnMouseExited(e -> card.setOpacity(1.0));
            card.setStyle(card.getStyle() +  "-fx-cursor: hand;");
            cardsRow.getChildren().add(card);
        }

        for(int i = 0; i < buildings.size(); i++){
            final int index = i;
            VBox card = buildBuildingCardPlaceholder(buildings.get(i), GamePhase.RESOLVING_OFFERS, i, true);
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

        if(gameState.getSkipAllowed()){
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

    // TESSERE OFFERTA

    private VBox buildOfferTile(OfferTileState tile, Map<String, String> nicknames, Map<String, String> colors, GamePhase phase){
        VBox box = new VBox(3);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPrefWidth(tileW);
        box.setMinHeight(tileH);

        String occupied = tile.getOccupiedByPlayerId();
        String borderColor = occupied != null ? colors.getOrDefault(occupied,  "#f5f0e8") : "#f5f0e840";
        String bgColor = occupied != null ? "rgba(255,255,255,0.08)" : "rgba(255,255,255,0.03)";

        box.setStyle("-fx-background-color: " + bgColor + ";" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: " + borderColor + ";" +
                "-fx-border-radius: 6;" +
                "-fx-border-width: 2;" +
                "-fx-padding: 5;");

        Label tileId = new Label(String.valueOf(tile.getTileId()));
        tileId.setStyle("-fx-text-fill: #f5f0e8; -fx-font-size: 14px; -fx-font-weight: bold;");

        // azione
        String action = buildActionText(tile);
        Label actionLabel = new Label(action);
        actionLabel.setStyle("-fx-text-fill: #f5f0e8; -fx-font-size: 14px;");
        actionLabel.setWrapText(true);

        // totem occupante
        if(occupied != null){
            Label totemLabel = new Label(nicknames.getOrDefault(occupied, "?"));
            totemLabel.setStyle("-fx-text-fill: " + borderColor + "; -fx-font-size: 8px; -fx-font-weight: bold;");
            box.getChildren().addAll(tileId, actionLabel, totemLabel);
        }else{
            box.getChildren().addAll(tileId, actionLabel);
        }

        boolean alreadyPlaced = lastState.getBoard().getOfferTiles().stream().anyMatch(t -> myPlayerId.equals(t.getOccupiedByPlayerId()));
        boolean canPlace = myPlayerId != null && phase == GamePhase.PLACING_TOTEMS && tile.getOccupiedByPlayerId() == null && !alreadyPlaced;

        if(canPlace){
            box.setStyle(box.getStyle() + "-fx-cursor: hand;");
            box.setOnMouseClicked(e -> view.placeTotem(tile.getTileId()));
            box.setOnMouseEntered(e -> box.setOpacity(0.75));
            box.setOnMouseExited(e -> box.setOpacity(1.0));
        }

        return box;
    }

    private String buildActionText(OfferTileState tile){
        StringBuilder sb = new StringBuilder();
        if(tile.getTopDrawCount() > 0)
            sb.append("↑").append(tile.getTopDrawCount()).append(" ");

        if(tile.getBottomDrawCount() > 0)
            sb.append("↓").append(tile.getBottomDrawCount()).append(" ");

        if(tile.getFoodReward() > 0)
            sb.append("+").append(tile.getFoodReward()).append("🍖");

        return sb.toString().trim();
    }

    private void showSkipDialog(){
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION
        );
        alert.setTitle("Nessuna carta disponibile.");
        alert.setHeaderText(null);
        alert.setContentText("Non ci sono carte pescabili. Vuoi saltare il turno?");

        javafx.scene.control.ButtonType skipButton = new javafx.scene.control.ButtonType("Salta turno");
        javafx.scene.control.ButtonType cancelButton = new javafx.scene.control.ButtonType(
                "Annulla", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE
        );
        alert.getButtonTypes().setAll(skipButton, cancelButton);

        alert.showAndWait().ifPresent(result -> {
            if(result == skipButton){
                view.skipTurn();
            }
        });
    }

    // TURN ORDER SLOT

    private HBox buildTurnOrderSlot(TurnOrderSlotState slot, Map<String, String> nicknames, Map<String, String> colors){
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 4, 2, 4));
        row.setMinHeight(16);

        String occupied = slot.getOccupiedByPlayerId();
        if (occupied != null){
            Rectangle dot = new Rectangle(8, 8);
            dot.setArcWidth(8);
            dot.setArcHeight(8);
            dot.setFill(Color.web(colors.getOrDefault(occupied, "#ffffff")));

            Label name = new Label(nicknames.getOrDefault(occupied, "?"));
            name.setStyle("-fx-text-fill: #f5f0e8; -fx-font-size: 9px;");

            row.getChildren().addAll(dot, name);

        }else{ // slot vuoto

            Rectangle dot = new Rectangle(8, 8);
            dot.setArcWidth(8);
            dot.setArcHeight(8);
            dot.setFill(Color.web("#ffffff30"));
            row.getChildren().add(dot);
        }

        if(slot.getFoodDelta() != 0){ // bonus cibo
            Label food = new Label((slot.getFoodDelta() > 0 ? "+" : "") + slot.getFoodDelta() + "🍖");
            food.setStyle("-fx-text-fill: #f1c400; -fx-font-size: 8px;");
            row.getChildren().add(food);
        }

        return row;
    }

    // BOARD DEI PLAYERS

    private void updatePlayers(List<PlayerState> players){
        playersContainer.getChildren().clear();
        for(PlayerState player : players){
            boolean isMe = player.getPlayerId().equals(myPlayerId);
            playersContainer.getChildren().add(buildPlayerPanel(player, isMe, playerPanelW));
        }
    }

    private VBox buildPlayerPanel(PlayerState player, boolean isMe, double width){
        String totemHex = resolveTotemColor(player.getTotemColor());

        VBox card = new VBox(5);
        card.setPrefWidth(width);
        card.setPadding(new Insets(8, 10, 8, 10));
        card.setStyle(
                "-fx-background-color: rgba(30,10,5,0.92);" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + totemHex + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-width: 3;"
        );

        // nome + TU (dove serve)
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

        // cibo + PP
        HBox resourcesRow = new HBox(10);
        resourcesRow.setAlignment(Pos.CENTER_LEFT);
        resourcesRow.getChildren().addAll(
                buildResource("🍖", String.valueOf(player.getFood())),
                buildResource("⭐", player.getPrestigePoints() + " PP")
        );

        // divisore
        Region div1 = new Region();
        div1.setPrefHeight(1);
        div1.setStyle("-fx-background-color: rgba(255,255,255,0.1);");

        // personaggi (ordine alfabetico in inglese)
        GridPane statsGrid = buildStatsGrid(player);

        // tot personaggi
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

        // building
        Region div3 = new Region();
        div3.setPrefHeight(1);
        div3.setStyle("-fx-background-color: rgba(255,255,255,0.08);");

        VBox buildingsSection = buildBuildingsSection(player.getBuildings());

        card.getChildren().addAll(nameRow, resourcesRow, div1, statsGrid, div2, totalRow, div3, buildingsSection);
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

        // numero per tipo + altre icone utili
        Map<CharacterType, Long> counts = new HashMap<>();
        int totalStars = 0;
        int totalDiscount = 0;
        Set<Object> inventionIcons = new HashSet<>();

        for (CardState card : player.getCharacters()) {
            if (card instanceof CharacterCardState c) {
                counts.merge(c.getCharacterType(), 1L, Long::sum);
                if (c.getCharacterType() == CharacterType.SHAMAN && c.getStars() != null)
                    totalStars += c.getStars();
                if (c.getCharacterType() == CharacterType.BUILDER && c.getDiscount() != null)
                    totalDiscount += c.getDiscount();
                if (c.getCharacterType() == CharacterType.INVENTOR && c.getInventionIcon() != null)
                    inventionIcons.add(c.getInventionIcon());
            }
        }

        // personaggi
        int finalTotalStars = totalStars;
        int finalTotalDiscount = totalDiscount;
        int differentIcons = inventionIcons.size();

        Object[][] rows = {
                {"🎨", "Artists",   counts.getOrDefault(CharacterType.ARTIST,    0L), null, null},
                {"🔧", "Builders",  counts.getOrDefault(CharacterType.BUILDER,   0L), "-" + finalTotalDiscount + "🍖", null},
                {"🏹", "Hunters",   counts.getOrDefault(CharacterType.HUNTER,    0L), null, null},
                {"💡", "Inventors", counts.getOrDefault(CharacterType.INVENTOR,  0L), null, "⬡" + differentIcons},
                {"🍓", "Gatherers", counts.getOrDefault(CharacterType.GATHERER,  0L), null, null},
                {"🔮", "Shamans",   counts.getOrDefault(CharacterType.SHAMAN,    0L), "★" + finalTotalStars, null},
        };

        for(int i=0; i<rows.length; i++){
            int col = (i%2)*3;
            int row = i/2;

            Label icon = new Label((String) rows[i][0]);
            icon.setStyle("-fx-font-size: 14px;");

            Label val = new Label(String.valueOf(rows[i][2]));
            val.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");

            grid.add(icon, col, row);
            grid.add(val, col +1, row);

            // info utili (quelle sopra)
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

    private VBox buildBuildingsSection(List<CardState> buildings){
        VBox section = new VBox(4);
        Label label = new Label("BUILDINGS");
        label.setStyle("-fx-text-fill: rgba(245,240,232,0.35); -fx-font-size: 10px;");

        HBox slotsRow = new HBox(4);
        slotsRow.setAlignment(Pos.CENTER_LEFT);
        slotsRow.setMinHeight(36);

        for(CardState b : buildings){
            VBox slot = new VBox();
            slot.setPrefSize(26, 36);
            slot.setAlignment(Pos.CENTER);
            slot.setStyle(  "-fx-background-color: rgba(180,120,50,0.6);" +
                    "-fx-background-radius: 4;" +
                    "-fx-border-color: rgba(255,200,100,0.3);" +
                    "-fx-border-radius: 4;" +
                    "-fx-border-width: 1;"
            );

            Label idLabel = new Label(b.getCardId().substring(0, Math.min(3, b.getCardId().length())));
            idLabel.setStyle("-fx-text-fill: #ffd; -fx-font-size: 7px;");
            slot.getChildren().add(idLabel);
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

    // -----------------------

    private String resolveTotemColor(String totemColor) {
        if (totemColor == null) return "#f5f0e8";
        if (totemColor.startsWith("#")) return totemColor;
        try {
            return TotemColors.valueOf(totemColor.toUpperCase()).getColor();
        } catch (IllegalArgumentException e) {
            return "#f5f0e8";
        }
    }

    private String cardColor(CardState card) {
        return switch (card.getCardKind()) {
            case CHARACTER -> {
                CharacterCardState c = (CharacterCardState) card;
                yield switch (c.getCharacterType()) {
                    case HUNTER -> "#c0392b";   // rosso
                    case GATHERER -> "#e67e22";   // arancio
                    case ARTIST -> "#d4a017";   // giallo dorato
                    case INVENTOR -> "#2980b9";   // azzurro
                    case SHAMAN -> "#8e44ad";   // viola
                    case BUILDER -> "#7a5230";   // marroncino
                    default -> "#5a2e10";
                };
            }
            case BUILDING -> "#808080";
            case EVENT -> "#5a2e10";
        };
    }

    private String cardTypeName(CardState card) {
        return switch (card.getCardKind()) {
            case CHARACTER -> {
                CharacterCardState c = (CharacterCardState) card;
                yield switch (c.getCharacterType()) {
                    case ARTIST   -> "ARTIST";
                    case BUILDER  -> "BUILDER";
                    case HUNTER   -> "HUNTER";
                    case INVENTOR -> "INVENTOR";
                    case GATHERER -> "GATHERER";
                    case SHAMAN   -> "SHAMAN";
                    default       -> "?";
                };
            }
            case BUILDING -> "BUILDING";
            case EVENT -> "EVENT";
        };
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
