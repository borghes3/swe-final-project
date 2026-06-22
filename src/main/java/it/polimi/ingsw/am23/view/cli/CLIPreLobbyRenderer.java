package it.polimi.ingsw.am23.view.cli;

import it.polimi.ingsw.am23.model.setup.PlayerConnectionInfo;
import it.polimi.ingsw.am23.network.LobbyState;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static it.polimi.ingsw.am23.view.cli.CLIColors.*;

final class CLIPreLobbyRenderer {

    private static final String TITLE_MARKER = "◆";

    void render(String playerName, String playerId, List<LobbyState> lobbies, String statusMessage, String currentLobbyId) {
        printBanner(playerName, playerId, statusMessage);
        printLobbyList(lobbies, currentLobbyId);
        printCommands();
    }

    private void printBanner(String playerName, String playerId, String statusMessage) {
        System.out.println(TITLE_MARKER + " MESOS " + TITLE_MARKER);
        System.out.println();
        System.out.println("player: " + safe(playerName));
        System.out.println("id: " + safe(playerId));
        System.out.println();
        if (statusMessage != null && !statusMessage.isBlank()) {
            System.out.println(paint(BR_GREEN, statusMessage));
        }
        System.out.println();
    }

    private void printLobbyList(List<LobbyState> lobbies, String currentLobbyId) {
        System.out.println(rule(80));
        System.out.println("LOBBIES");
        System.out.println(rule(80));
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
        String nameColor = current ? BR_CYAN : BR_WHITE;  // cyan se current, altrimenti white

        String occupancy = lobby.isFull() ? paint(BR_RED, "FULL") : lobby.getCurrentPlayers() + "/" + lobby.getMaxPlayers();
        String owner = lobby.getPlayers().stream()
                .filter(p -> Objects.equals(p.id(), lobby.getOwnerPlayerId()))
                .map(PlayerConnectionInfo::nickname)
                .findFirst()
                .orElse(safe(lobby.getOwnerPlayerId()));
        String title = paint(nameColor, lobby.getLobbyName()) + "  " + paint(nameColor, "[" + lobby.getLobbyId() + "]");
        String players = lobby.getPlayers().isEmpty()
                ? "empty"
                : lobby.getPlayers().stream().map(PlayerConnectionInfo::nickname).collect(Collectors.joining(", "));

        System.out.println("  " + title + "   " + occupancy + "   owner: " + (current ? paint(nameColor, owner) : owner));
        System.out.println("    " + paint(DIM, "players: " + players));
    }

    private void printCommands() {
        System.out.println();
        System.out.println(paint(DIM, rule(80)));
        System.out.println(paint(DIM, "  refresh  |"
                + "  create <name>  |"
                + "  join  <code>  |"
                + "  start  |"
                + "  quit"));
        System.out.println(paint(DIM, rule(80)));
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "n/a" : value;
    }
}
