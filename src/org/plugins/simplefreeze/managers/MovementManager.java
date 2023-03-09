package org.plugins.simplefreeze.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.objects.players.FrozenPlayer;

import java.util.Map;
import java.util.UUID;

/**
 * Created by Rory on 2/11/2017.
 */
public class MovementManager {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;

    private BukkitTask task;
    private boolean headMovementBoolean;

    public MovementManager(SimpleFreezeMain plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.setHeadMovementBoolean(plugin.getConfig().getBoolean("head-movement"));
        this.startTask();
    }

    public void setHeadMovementBoolean(boolean headMovementBoolean) {
        this.headMovementBoolean = headMovementBoolean;
    }

    public void startTask() {
        if (this.task == null) {
            this.task = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Map.Entry<UUID, FrozenPlayer> entry : playerManager.getFrozenPlayers().entrySet()) {
                        Player p = Bukkit.getPlayer(entry.getKey());
                        Location pLoc = p.getLocation();
                        Location freezeLoc = entry.getValue().getFreezeLoc().clone();
                        if (!headMovementBoolean) {
                            p.teleport(freezeLoc);
                        } else if (pLoc.distanceSquared(freezeLoc) > 0) {
                            freezeLoc.setYaw(pLoc.getYaw());
                            freezeLoc.setPitch(pLoc.getPitch());
                            p.teleport(freezeLoc);
                        }
                    }
                }
            }.runTaskTimer(this.plugin, 4L, 4L);
        }
    }

    public void endTask() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

}
