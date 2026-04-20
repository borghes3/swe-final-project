package it.polimi.ingsw.am23.network.rmi.server;

import it.polimi.ingsw.am23.controller.GameController;

public final class RmiServerLauncher {

    private RmiServerLauncher() {
    }

    public static void main(String[] args) throws Exception {
        GameController controller = new GameController();
        RmiServer.startRmiServer(controller);
    }
}
