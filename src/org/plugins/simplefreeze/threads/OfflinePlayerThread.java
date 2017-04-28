package org.plugins.simplefreeze.threads;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * Created by Rory on 4/24/2017.
 */
public class OfflinePlayerThread extends Thread {

    private final String name;

    private UUID uuid;
    private boolean done = false;

    public OfflinePlayerThread(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        OfflinePlayer player = Bukkit.getOfflinePlayer(this.name);
        if (player.hasPlayedBefore()) {
            this.uuid = player.getUniqueId();
        } else {
            this.uuid = null;
        }
        this.done = true;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public boolean isDone() {
        return this.done;
    }

}
