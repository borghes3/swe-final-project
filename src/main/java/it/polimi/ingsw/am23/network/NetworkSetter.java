package it.polimi.ingsw.am23.network;

import it.polimi.ingsw.am23.network.rmi.client.HeartbeatService;
import it.polimi.ingsw.am23.network.rmi.client.RmiClient;
import it.polimi.ingsw.am23.network.rmi.client.VirtualServerRmi;
import it.polimi.ingsw.am23.network.socket.client.SocketClient;

public class NetworkSetter {

    private static HeartbeatService heartbeatService;

    public static VirtualServer connect(String host, String playerName, VirtualView view, String type) throws Exception {
        return switch (type) {
            case "RMI" -> {
                VirtualServerRmi server = RmiClient.connect(host, playerName, view);
                heartbeatService = new HeartbeatService(server, () -> {
                    try {
                        view.onServerCrashed();
                    } catch (Exception ignored) {}
                });
                heartbeatService.start();
                yield server;
            }
            case "SOCKET" -> {
                VirtualServer server = SocketClient.connectToServer(host, view);
                server.connect(playerName, view);
                yield server;
            }
            default -> throw new Exception("Tipo di connessione non valido: " + type + ". Usare RMI o SOCKET.");
        };
    }

    public static void stopHeartbeat(){
        if(heartbeatService != null){
            heartbeatService.stop();
            heartbeatService = null;
        }
    }
}
