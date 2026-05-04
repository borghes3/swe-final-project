package it.polimi.ingsw.am23.view.cli;

import it.polimi.ingsw.am23.model.state.BoardState;
import it.polimi.ingsw.am23.model.state.CardState;
import it.polimi.ingsw.am23.model.state.GameState;
import it.polimi.ingsw.am23.model.state.OfferTileState;
import it.polimi.ingsw.am23.model.state.PlayerState;
import it.polimi.ingsw.am23.model.state.TurnOrderSlotState;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class CLIBoardRenderer {

    private static final String TITLE_MARKER = "◆";
    private static final String YOU_MARKER_LEFT = "«";
    private static final String YOU_MARKER_RIGHT = "»";

    void render(GameState gameState, String statusMessage, String localPlayerId) {
        if (gameState == null) {
            System.out.println("Game state unavailable.");
            return;
        }

        Map<String, String> playerNamesById = gameState.getPlayers().stream()
                .filter(player -> player.getPlayerId() != null)
                .collect(Collectors.toMap(
                        PlayerState::getPlayerId,
                        PlayerState::getNickname,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        BoardState board = gameState.getBoard();

        System.out.println(center(TITLE_MARKER + " Game State " + TITLE_MARKER));
        System.out.println(center("Phase: " + gameState.getPhase().name() + " | Era: " + gameState.getCurrentEra().name() + " | Round: " + gameState.getCurrentRound()));
        System.out.println(center("You: " + highlightIfLocal(displayName(localPlayerId, playerNamesById), localPlayerId, localPlayerId)));
        System.out.println(center("Current player: " + highlightIfLocal(displayName(gameState.getCurrentPlayerId(), playerNamesById), gameState.getCurrentPlayerId(), localPlayerId)));
        System.out.println();

        // Turn order
        System.out.println("=== Turn order ===");
        if (board == null || board.getTurnOrderSlots().isEmpty()) {
            System.out.println("  Empty");
        } else {
            board.getTurnOrderSlots().stream()
                    .sorted(Comparator.comparingInt(TurnOrderSlotState::getPositionIndex))
                    .forEach(slot -> System.out.println("  #" + slot.getPositionIndex() + ": " + displayName(slot.getOccupiedByPlayerId(), playerNamesById)));
        }

        System.out.println();

        // Offer track
        System.out.println("=== Offer track ===");
        if (board == null || board.getOfferTiles().isEmpty()) {
            System.out.println("  Empty");
        } else {
            board.getOfferTiles().stream()
                    .sorted(Comparator.comparing(OfferTileState::getTileId))
                    .forEach(tile -> System.out.println("  [" + tile.getTileId() + "] owner=" + displayName(tile.getOccupiedByPlayerId(), playerNamesById)
                            + " draw=" + tile.getTopDrawCount() + "/" + tile.getBottomDrawCount() + " food=" + tile.getFoodReward()));
        }

        System.out.println();

        // Rows
        printCardList("Top cards", board, "top", true);
        printCardList("Bottom cards", board, "bottom", true);
        printCardList("Top buildings", board, "top", false);
        printCardList("Bottom buildings", board, "bottom", false);

        printStatus(statusMessage);
    }

    private void printStatus(String statusMessage) {
        if (statusMessage == null || statusMessage.isBlank()) {
            return;
        }

        System.out.println();
        System.out.println(statusMessage);
    }

    private String displayName(String playerId, Map<String, String> playerNamesById) {
        if (playerId == null || playerId.isBlank()) {
            return "free";
        }

        String playerName = playerNamesById.get(playerId);
        return playerName == null || playerName.isBlank() ? playerId : playerName;
    }

    private String highlightIfLocal(String value, String candidatePlayerId, String localPlayerId) {
        if (candidatePlayerId != null && candidatePlayerId.equals(localPlayerId)) {
            return YOU_MARKER_LEFT + value + YOU_MARKER_RIGHT;
        }
        return value;
    }

    private String center(String value) {
        return value;
    }

    private void printCardList(String title, BoardState board, String rowType, boolean isCards) {
        if (board == null) {
            return;
        }

        List<? extends CardState> cards;
        if (isCards) {
            cards = rowType.equals("top") ? board.getTopRow() : board.getBottomRow();
        } else {
            cards = rowType.equals("top") ? board.getTopBuildings() : board.getBottomBuildings();
        }

        System.out.print(title + ": ");
        if (cards.isEmpty()) {
            System.out.println("(empty)");
        } else {
            String cardIds = cards.stream()
                    .map(CardState::getCardId)
                    .collect(Collectors.joining(", "));
            System.out.println(cardIds);
        }
    }
}
