package it.polimi.ingsw.am23.network;

import it.polimi.ingsw.am23.network.rmi.client.HeartbeatService;
import it.polimi.ingsw.am23.network.rmi.client.RmiClient;
import it.polimi.ingsw.am23.network.rmi.client.VirtualServerRmi;
import it.polimi.ingsw.am23.network.socket.client.SocketClient;

/**
 * Helper used by the client side to establish a connection over the
 * desired transport (RMI or socket). Also owns the heartbeat service for
 * the RMI transport.
 */
public class NetworkSetter {

    private static HeartbeatService heartbeatService;

    /**
     * Connects the client to the server using the requested transport.
     *
     * @param host       host of the server
     * @param playerName desired display nickname
     * @param view       the local {@link VirtualView} the server will push to
     * @param type       transport name ({@code "RMI"} or {@code "SOCKET"})
     * @return the remote {@link VirtualServer} stub
     * @throws Exception on transport failure or for unsupported transports
     */
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
            default -> throw new Exception("Invalid connection type: " + type + ". Use RMI or SOCKET.");
        };
    }

    /**
     * Stops the heartbeat thread, if any was started.
     */
    public static void stopHeartbeat(){
        if(heartbeatService != null){
            heartbeatService.stop();
            heartbeatService = null;
        }
    }
}
