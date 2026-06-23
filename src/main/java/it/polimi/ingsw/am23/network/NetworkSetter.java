package it.polimi.ingsw.am23.network;

import it.polimi.ingsw.am23.network.rmi.client.RmiClient;
import it.polimi.ingsw.am23.network.rmi.client.VirtualServerRmi;
import it.polimi.ingsw.am23.network.socket.client.SocketClient;

/**
 * Helper used by the client side to establish a connection over the
 * desired transport (RMI or socket).
 */
public class NetworkSetter {


    /**
     * Connects the client to the server using the requested transport.
     *
     * @param host             host of the server
     * @param playerName       desired display nickname
     * @param view             the local {@link VirtualView} the server will push to
     * @param type             transport name ({@code "RMI"} or {@code "SOCKET"})
     * @param rmiCallbackPort  TCP port the RMI client exports its callback object on;
     *                         {@code 0} lets RMI pick a free random port. Ignored
     *                         when {@code type} is {@code "SOCKET"}
     * @return the remote {@link VirtualServer} stub
     * @throws Exception on transport failure or for unsupported transports
     */
    public static VirtualServer connect(String host, String playerName, VirtualView view, String type, int rmiCallbackPort) throws Exception {
        return switch (type) {
            case "RMI" -> {
                VirtualServerRmi server = RmiClient.connect(host, playerName, view, rmiCallbackPort);
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
     * Stops the RMI heartbeat thread, if any was started.
     */
    public static void stopHeartbeat() {
        RmiClient.stopHeartbeat();
    }
}
