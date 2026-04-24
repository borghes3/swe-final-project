package it.polimi.ingsw.am23.network;

import it.polimi.ingsw.am23.network.rmi.client.RmiClient;
import it.polimi.ingsw.am23.network.socket.client.SocketClient;

public class NetworkSetter {
    public static VirtualServer connect(String host, String playerName, VirtualView view, String type) throws Exception {
        return switch (type) {
            case "RMI" -> RmiClient.connect(host, playerName, view);
            case "SOCKET" -> {
                VirtualServer server = SocketClient.connectToServer(host, view);
                server.connect(playerName, view);
                yield server;
            }
            default -> throw new Exception("Tipo di connessione non valido: " + type + ". Usare RMI o SOCKET.");
        };
    }
}
