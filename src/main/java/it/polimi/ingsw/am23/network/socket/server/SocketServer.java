package it.polimi.ingsw.am23.network.socket.server;

import it.polimi.ingsw.am23.network.VirtualServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class SocketServer implements Runnable {

    private static final int PORT = 1235;

    private final VirtualServer serverController;

    public SocketServer(VirtualServer serverController) {
        this.serverController = serverController;
    }

    // creation of server
    public static void startSocketServer(VirtualServer serverController) {
        SocketServer server = new SocketServer(serverController);
        Thread thread = new Thread(server, "SocketServerName");
        thread.start();
        System.out.println("Socket Server avviato su porta " + PORT);
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuova connessione da: " + clientSocket.getRemoteSocketAddress());
                try {
                    Connector handler = new Connector(clientSocket, serverController);
                    Thread handlerThread = new Thread(handler, "client-" + clientSocket.getRemoteSocketAddress());
                    handlerThread.start();
                } catch (IOException e) {
                    System.err.println("SocketServer: errore durante la creazione del ClientHandler – " + e.getMessage());
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            System.err.println("SocketServer: errore fatale – " + e.getMessage());
        }
    }
}
