package it.polimi.ingsw.am23.network.socket.server;

import it.polimi.ingsw.am23.controller.GameController;

public final class SocketServerLauncher {

    private SocketServerLauncher() {}

    public static void main(String[] args) {
        GameController controller = new GameController();
        SocketServer.startSocketServer(controller);
    }
}
