package it.polimi.ingsw.am23.network.rmi.server;

public final class RmiServerLauncher {

    private RmiServerLauncher() {
    }

    public static void main(String[] args) throws Exception {
        InMemoryVirtualServer controller = new InMemoryVirtualServer();
        RmiServer.startRmiServer(controller);
    }
}
