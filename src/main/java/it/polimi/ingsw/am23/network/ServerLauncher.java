package it.polimi.ingsw.am23.network;

import it.polimi.ingsw.am23.controller.GameController;
import it.polimi.ingsw.am23.network.rmi.server.RmiServer;
import it.polimi.ingsw.am23.network.socket.server.SocketServer;

public class ServerLauncher {

    public static void main(String[] args) throws Exception{
        GameController controller = new GameController();
        RmiServer.startRmiServer(controller);
        SocketServer.startSocketServer(controller);
    }
}
