package org.plugins.simplefreeze.managers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.plugins.simplefreeze.SimpleFreezeMain;
import roryslibrary.util.TimeUtil;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Rory on 3/11/2017.
 */
public class MessageManager {

    private final SimpleFreezeMain plugin;

    private HashMap<UUID, BukkitTask> freezePlayers = new HashMap<>();
    private HashMap<UUID, BukkitTask> freezeLocPlayers = new HashMap<>();
    private HashMap<UUID, BukkitTask> tempFreezePlayers = new HashMap<>();
    private HashMap<UUID, BukkitTask> tempFreezeLocPlayers = new HashMap<>();
    private HashMap<UUID, BukkitTask> freezeAllPlayers = new HashMap<>();
    private HashMap<UUID, BukkitTask> freezeAllLocPlayers = new HashMap<>();

    private int freezeInterval;
    private int freezeLocInterval;
    private int tempFreezeInterval;
    private int tempFreezeLocInterval;
    private int freezeAllInterval;
    private int freezeAllLocInterval;

    public MessageManager(SimpleFreezeMain plugin) {
        this.plugin = plugin;
        this.freezeInterval = this.plugin.getConfig().getInt("message-intervals.freeze");
        this.freezeLocInterval = this.plugin.getConfig().getInt("message-intervals.freeze-location");
        this.tempFreezeInterval = this.plugin.getConfig().getInt("message-intervals.temp-freeze");
        this.tempFreezeLocInterval = this.plugin.getConfig().getInt("message-intervals.temp-freeze-location");
        this.freezeAllInterval = this.plugin.getConfig().getInt("message-intervals.freezeall");
        this.freezeAllLocInterval = this.plugin.getConfig().getInt("message-intervals.freezeall-location");
    }

    public void addFreezePlayer(Player p, String msg) {
        this.freezePlayers.put(p.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                p.sendMessage(plugin.placeholders(msg));
            }
        }.runTaskTimer(this.plugin, this.freezeInterval * 20L, this.freezeInterval * 20L));
    }

    public void addFreezeLocPlayer(Player p, String msg) {
        this.freezeLocPlayers.put(p.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                p.sendMessage(plugin.placeholders(msg));
            }
        }.runTaskTimer(this.plugin, this.freezeLocInterval * 20L, this.freezeLocInterval * 20L));
    }

    public void addTempFreezePlayer(Player p, String msg) {
        this.tempFreezePlayers.put(p.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                String newMsg = msg.replace("{TIME}", TimeUtil.formatTime((plugin.getPlayerConfig().getConfig().getLong("players." + p.getUniqueId().toString() + ".unfreeze-date") - System.currentTimeMillis())/1000L));
                p.sendMessage(plugin.placeholders(newMsg));
            }
        }.runTaskTimer(this.plugin, this.tempFreezeInterval * 20L, this.tempFreezeInterval * 20L));
    }

    public void addTempFreezeLocPlayer(Player p, String msg) {
        this.tempFreezeLocPlayers.put(p.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                String newMsg = msg.replace("{TIME}", TimeUtil.formatTime((plugin.getPlayerConfig().getConfig().getLong("players." + p.getUniqueId().toString() + ".unfreeze-date") - System.currentTimeMillis())/1000L));
                p.sendMessage(plugin.placeholders(newMsg));
            }
        }.runTaskTimer(this.plugin, this.tempFreezeLocInterval * 20L, this.tempFreezeLocInterval * 20L));
    }

    public void addFreezeAllPlayer(Player p, String msg) {
        this.freezeAllPlayers.put(p.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                p.sendMessage(plugin.placeholders(msg));
            }
        }.runTaskTimer(this.plugin, this.freezeAllInterval * 20L, this.freezeAllInterval * 20L));
    }

    public void addFreezeAllLocPlayer(Player p, String msg) {
        this.freezeAllLocPlayers.put(p.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                p.sendMessage(plugin.placeholders(msg));
            }
        }.runTaskTimer(this.plugin, this.freezeAllLocInterval * 20L, this.freezeAllLocInterval * 20L));
    }

    public void removePlayer(Player p) {
        if (this.freezePlayers.containsKey(p.getUniqueId())) {
            this.freezePlayers.remove(p.getUniqueId()).cancel();

        } else if (this.freezeLocPlayers.containsKey(p.getUniqueId())) {
            this.freezeLocPlayers.remove(p.getUniqueId()).cancel();

        } else if (this.tempFreezePlayers.containsKey(p.getUniqueId())) {
            this.tempFreezePlayers.remove(p.getUniqueId()).cancel();

        } else if (this.tempFreezeLocPlayers.containsKey(p.getUniqueId())) {
            this.tempFreezeLocPlayers.remove(p.getUniqueId()).cancel();

        } else if (this.freezeAllPlayers.containsKey(p.getUniqueId())) {
            this.freezeAllPlayers.remove(p.getUniqueId()).cancel();

        } else if (this.freezeAllLocPlayers.containsKey(p.getUniqueId())) {
            this.freezeAllLocPlayers.remove(p.getUniqueId()).cancel();

        }
    }

    public void clearFreezeAllPlayers() {
        for (UUID uuid : this.freezeAllLocPlayers.keySet()) {
            this.freezeAllLocPlayers.get(uuid).cancel();
        }

        for (UUID uuid : this.freezeAllPlayers.keySet()) {
            this.freezeAllPlayers.get(uuid).cancel();
        }

        this.freezeAllLocPlayers.clear();
        this.freezeAllPlayers.clear();
    }

    public int getFreezeInterval() {
        return freezeInterval;
    }

    public void setFreezeInterval(int freezeInterval) {
        this.freezeInterval = freezeInterval;
    }

    public int getFreezeLocInterval() {
        return freezeLocInterval;
    }

    public void setFreezeLocInterval(int freezeLocInterval) {
        this.freezeLocInterval = freezeLocInterval;
    }

    public int getTempFreezeInterval() {
        return tempFreezeInterval;
    }

    public void setTempFreezeInterval(int tempFreezeInterval) {
        this.tempFreezeInterval = tempFreezeInterval;
    }

    public int getTempFreezeLocInterval() {
        return tempFreezeLocInterval;
    }

    public void setTempFreezeLocInterval(int tempFreezeLocInterval) {
        this.tempFreezeLocInterval = tempFreezeLocInterval;
    }

    public int getFreezeAllInterval() {
        return freezeAllInterval;
    }

    public void setFreezeAllInterval(int freezeAllInterval) {
        this.freezeAllInterval = freezeAllInterval;
    }

    public int getFreezeAllLocInterval() {
        return freezeAllLocInterval;
    }

    public void setFreezeAllLocInterval(int freezeAllLocInterval) {
        this.freezeAllLocInterval = freezeAllLocInterval;
    }
}
