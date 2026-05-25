package it.polimi.ingsw.am23.view.cli;

import it.polimi.ingsw.am23.model.setup.PlayerConnectionInfo;
import it.polimi.ingsw.am23.network.LobbyState;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

final class CLIPreLobbyRenderer {

    private static final String TITLE_MARKER = "◆";
    private static final String CURRENT_MARKER = "»";

    void render(String playerName, String playerId, List<LobbyState> lobbies, String statusMessage, String currentLobbyId) {
        printBanner(playerName, playerId, statusMessage);
        printLobbyList(lobbies, currentLobbyId);
        printCommands();
    }

    private void printBanner(String playerName, String playerId, String statusMessage) {
        System.out.println(center(TITLE_MARKER + " Mesos CLI Client " + TITLE_MARKER));
        System.out.println(center("Pre-lobby"));
        System.out.println(center("User: " + highlight(playerName)));
        System.out.println(center("Id: " + safe(playerId)));
        if (statusMessage != null && !statusMessage.isBlank()) {
            System.out.println(center(statusMessage));
        }
        System.out.println();
    }

    private void printLobbyList(List<LobbyState> lobbies, String currentLobbyId) {
        System.out.println("=== Available lobbies ===");
        if (lobbies == null || lobbies.isEmpty()) {
            System.out.println("No lobbies available.");
            System.out.println();
            return;
        }

        for (LobbyState lobby : lobbies) {
            printLobby(lobby, Objects.equals(lobby.getLobbyId(), currentLobbyId));
        }
        System.out.println();
    }

    private void printLobby(LobbyState lobby, boolean current) {
        String title = lobby.getLobbyName() + " [" + lobby.getLobbyId() + "]";
        if (current) {
            title += " " + CURRENT_MARKER;
        }

        String players = lobby.getPlayers().isEmpty()
                ? "empty"
                : lobby.getPlayers().stream().map(PlayerConnectionInfo::getNickname).collect(Collectors.joining(", "));
        String occupancy = lobby.isFull() ? "full" : "free";
        String owner = lobby.getOwnerPlayerId() == null ? "unknown" : lobby.getOwnerPlayerId();
        String count = lobby.getCurrentPlayers() + "/" + lobby.getMaxPlayers();

        System.out.println("  " + title + " - " + count + " - " + occupancy + " - owner: " + owner);
        System.out.println("    players: " + players);
    }

    private void printCommands() {
        System.out.println("=== Commands ===");
        System.out.println("  refresh - updates the lobby list");
        System.out.println("  create <lobby-name>");
        System.out.println("  join <code>");
        System.out.println("  leave");
        System.out.println("  start");
        System.out.println("  quit");
    }

    private String highlight(String value) {
        if (value == null || value.isBlank()) {
            return "n/a";
        }
        return CURRENT_MARKER + value + CURRENT_MARKER;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "n/a" : value;
    }

    private String center(String value) {
        return value;
    }
}
