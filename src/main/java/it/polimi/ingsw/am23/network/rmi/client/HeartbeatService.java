package it.polimi.ingsw.am23.network.rmi.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * RMI-only heartbeat service.
 * Periodically sends the connected player id to the server so the server
 * can detect involuntary client disconnections.
 */
public class HeartbeatService {

    private final VirtualServerRmi server;
    private final String playerId;
    private final Runnable onServerDisconnected;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "rmi-heartbeat");
        t.setDaemon(true);
        return t;
    });
    private volatile boolean stopped = false;

    /**
     * Builds a heartbeat service for a connected RMI player.
     *
     * @param server               remote RMI server
     * @param playerId             id of the connected player
     * @param onServerDisconnected callback invoked if the server cannot be reached
     */
    public HeartbeatService(VirtualServerRmi server, String playerId, Runnable onServerDisconnected) {
        this.server = server;
        this.playerId = playerId;
        this.onServerDisconnected = onServerDisconnected;
    }

    /**
     * Starts sending heartbeat pings at a fixed cadence.
     */
    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            if (stopped) {
                return;
            }

            try {
                server.ping(playerId);
            } catch (Exception e) {
                stopped = true;
                scheduler.shutdownNow();
                onServerDisconnected.run();
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    /**
     * Stops the heartbeat scheduler.
     */
    public void stop() {
        stopped = true;
        scheduler.shutdownNow();
    }
}
