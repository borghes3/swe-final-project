package it.polimi.ingsw.am23.network.rmi.client;

import it.polimi.ingsw.am23.network.VirtualServer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatService {

    private final VirtualServer server;
    private final Runnable onDisconnected;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "heartbeat");
        t.setDaemon(true);
        return t;
    });
    private volatile boolean stopped = false;

    public HeartbeatService(VirtualServer server, Runnable onDisconnected){
        this.server = server;
        this.onDisconnected = onDisconnected;
    }

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

    public void stop() {
        stopped = true;
        scheduler.shutdown();
    }
}