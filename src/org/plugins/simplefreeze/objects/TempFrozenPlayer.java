package org.plugins.simplefreeze.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.plugins.simplefreeze.SimpleFreezeMain;

import java.util.UUID;

public class TempFrozenPlayer extends FrozenPlayer {

    private final Long unfreezeDate;

    private BukkitTask task = null;

    public TempFrozenPlayer(Long date, Long unfreezeDate, UUID freezeeUUID, UUID freezerUUID, Location originalLoc, Location freezeLoc, boolean sqlFreeze, ItemStack helmet) {
        super(date, freezeeUUID, freezerUUID, originalLoc, freezeLoc, sqlFreeze, helmet);
        this.unfreezeDate = unfreezeDate;
    }

    public TempFrozenPlayer(Long date, Long unfreezeDate, UUID freezeeUUID, UUID freezerUUID, Location originalLoc, Location freezeLoc, boolean sqlFreeze) {
        super(date, freezeeUUID, freezerUUID, originalLoc, freezeLoc, sqlFreeze);
        this.unfreezeDate = unfreezeDate;

    }

    public Long getUnfreezeDate() {
        return unfreezeDate;
    }

    public void startTask(final SimpleFreezeMain plugin) {
        final TempFrozenPlayer tempFrozenPlayer = this;
        if (!this.isSqlFreeze()) {
            this.task = new BukkitRunnable() {
                @Override
                public void run() {
                    Player p = Bukkit.getPlayer(getFreezeeUUID());
                    if (p != null) {
                        for (String msg : plugin.getConfig().getStringList("unfreeze-message")) {
                            p.sendMessage(plugin.placeholders(msg).replace("{PLAYER}", tempFrozenPlayer.getFreezeeName()));
                        }
                        p.getInventory().setHelmet(tempFrozenPlayer.getHelmet());
                        p.teleport(tempFrozenPlayer.getOriginalLoc());
                        if (plugin.getConfig().getBoolean("enable-fly")) {
                            p.setAllowFlight(false);
                            p.setFlying(false);
                        }
                    }
                    plugin.getPlayerConfig().getConfig().set("players." + tempFrozenPlayer.getFreezeeUUID().toString(), null);
                    plugin.getPlayerConfig().saveConfig();
                    plugin.getPlayerConfig().reloadConfig();
                    plugin.getPlayerManager().removeFrozenPlayer(tempFrozenPlayer.getFreezeeUUID());

                }
            }.runTaskLater(plugin, (tempFrozenPlayer.getUnfreezeDate() - System.currentTimeMillis()) / 1000L * 20L);

        } else {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    //SQL TABLE STUFF
                }
            }.runTaskLater(plugin, (tempFrozenPlayer.getUnfreezeDate() - System.currentTimeMillis()) / 1000L * 20L);
        }
    }

    public void cancelTask() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

}
