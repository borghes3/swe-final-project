package it.polimi.ingsw.am23.network;

import it.polimi.ingsw.am23.controller.GameController;
import it.polimi.ingsw.am23.network.rmi.server.RmiServer;
import it.polimi.ingsw.am23.network.socket.server.SocketServer;

/**
 * Entry point of the server application. Bootstraps a single {@link
 * GameController} instance and exposes it through both the RMI and the
 * socket transport.
 */
public class ServerLauncher {

    /**
     * Boots the server.
     *
     * @param args command line arguments (currently unused)
     * @throws Exception if either transport fails to start
     */
    public static void main(String[] args) throws Exception{
        GameController controller = new GameController();
        RmiServer.startRmiServer(controller);
        SocketServer.startSocketServer(controller);
    }
}
