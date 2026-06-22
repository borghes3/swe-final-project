package it.polimi.ingsw.am23.network.rmi.client;

import it.polimi.ingsw.am23.network.VirtualServer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Schedules a periodic ping toward the supplied RMI server to detect
 * connection drops. On failure, the supplied callback is invoked and the
 * service stops.
 */
public class HeartbeatService {

    private final VirtualServer server;
    private final Runnable onDisconnected;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "heartbeat");
        t.setDaemon(true);
        return t;
    });
    private volatile boolean stopped = false;

    /**
     * Builds a new heartbeat service.
     *
     * @param server         remote server to ping
     * @param onDisconnected callback invoked on a failed ping
     */
    public HeartbeatService(VirtualServer server, Runnable onDisconnected){
        this.server = server;
        this.onDisconnected = onDisconnected;
    }

    /** Starts pinging the server at a fixed cadence of 2 seconds. */
    public void start(){
        scheduler.scheduleAtFixedRate(() -> {
            if(stopped) return;
            try{
                server.ping();
            } catch (Exception e){
                stopped = true;
                scheduler.shutdown();
                onDisconnected.run();
            }
        }, 2, 2, TimeUnit.SECONDS);
    }

    /** Stops the heartbeat scheduler. */
    public void stop() {
        stopped = true;
        scheduler.shutdown();
    }
}
