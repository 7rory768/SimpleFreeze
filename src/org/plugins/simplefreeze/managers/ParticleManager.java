package org.plugins.simplefreeze.managers;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.plugins.simplefreeze.SimpleFreezeMain;

import java.util.UUID;

/**
 * Created by Rory on 1/30/2017.
 */
public class ParticleManager {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;
    private static BukkitTask task;

    private String effectName;
    private int radius;

    public ParticleManager(SimpleFreezeMain plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.effectName = this.plugin.getConfig().getString("frozen-particles.particle", "null");
        try {
            Effect.valueOf(this.effectName);
            this.startTask();
        }
        catch (IllegalArgumentException e) {

        }
        this.radius = this.plugin.getConfig().getInt("frozen-particles.radius");
    }

    public void setEffect(String effectName) {
        this.effectName = effectName;
        try {
            Effect.valueOf(this.effectName);
            if (ParticleManager.task != null) {
                this.startTask();
            }
        }
        catch (IllegalArgumentException e) {
            if (ParticleManager.task != null) {
                this.endTask();
            }
        }
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void startTask() {
        if (this.task == null) {
            this.task = new BukkitRunnable() {
                @Override
                public void run() {
                    for (UUID uuid : playerManager.getFrozenPlayers().keySet()) {
                        Player p = Bukkit.getPlayer(uuid);
                        p.getWorld().playEffect(p.getLocation().clone().add(0, 2, 0), Effect.valueOf(effectName), radius);
                    }
                }
            }.runTaskTimer(this.plugin, 0L, 5L);
        }
    }

    public void endTask() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

}
