package it.polimi.ingsw.am23.network.rmi.server;

import it.polimi.ingsw.am23.controller.GameController;

/**
 * Standalone entry point that boots only the RMI transport.
 */
public final class RmiServerLauncher {

    private RmiServerLauncher() {
    }

    /**
     * Boots the RMI server with a fresh {@link GameController}.
     *
     * @param args command line arguments (currently unused)
     * @throws Exception on bootstrap failure
     */
    public static void main(String[] args) throws Exception {
        GameController controller = new GameController();
        RmiServer.startRmiServer(controller);
    }
}
