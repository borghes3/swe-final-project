package it.polimi.ingsw.am23.network.socket.server;

import it.polimi.ingsw.am23.controller.GameController;

/**
 * Standalone entry point that boots only the socket transport.
 */
public final class SocketServerLauncher {

    private SocketServerLauncher() {
    }

    /**
     * Boots the socket server with a fresh {@link GameController}.
     *
     * @param args command line arguments (currently unused)
     */
    public static void main(String[] args) {
        GameController controller = new GameController();
        SocketServer.startSocketServer(controller);
    }
}
