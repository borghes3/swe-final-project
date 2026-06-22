package it.polimi.ingsw.am23.network.socket.server;

import it.polimi.ingsw.am23.network.VirtualServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server-side socket acceptor. Listens on a well-known port and spawns a
 * {@link Connector} thread for every accepted connection.
 */
public final class SocketServer implements Runnable {

    private static final int PORT = 1235;

    private final VirtualServer serverController;

    /**
     * Builds a new acceptor bound to the supplied controller.
     *
     * @param serverController controller the connectors delegate to
     */
    public SocketServer(VirtualServer serverController) {
        this.serverController = serverController;
    }

    /**
     * Boots the acceptor on a dedicated daemon-less thread.
     *
     * @param serverController controller the connectors delegate to
     */
    public static void startSocketServer(VirtualServer serverController) {
        SocketServer server = new SocketServer(serverController);
        Thread thread = new Thread(server, "SocketServerName");
        thread.start();
        System.out.println("Socket Server started on port " + PORT);
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from: " + clientSocket.getRemoteSocketAddress());
                try {
                    Connector handler = new Connector(clientSocket, serverController);
                    Thread handlerThread = new Thread(handler, "client-" + clientSocket.getRemoteSocketAddress());
                    handlerThread.start();
                } catch (IOException e) {
                    System.err.println("SocketServer: error while creating the ClientHandler – " + e.getMessage());
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            System.err.println("SocketServer: fatal error – " + e.getMessage());
        }
    }
}
