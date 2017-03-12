package org.plugins.simplefreeze.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.LocationManager;
import org.plugins.simplefreeze.managers.PlayerManager;

import java.util.List;
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
        final PlayerManager playerManager = plugin.getPlayerManager();
        final LocationManager locationManager = plugin.getLocationManager();

        if (!this.isSqlFreeze()) {
            this.task = new BukkitRunnable() {
                @Override
                public void run() {
                    UUID uuid = getFreezeeUUID();
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        for (String msg : plugin.getConfig().getStringList("unfreeze-message")) {
                            p.sendMessage(plugin.placeholders(msg).replace("{PLAYER}", tempFrozenPlayer.getFreezeeName()).replace("{UNFREEZER}", "CONSOLE"));
                        }

                        p.getInventory().setHelmet(tempFrozenPlayer.getHelmet());

                        Location originalLoc =  tempFrozenPlayer.getOriginalLoc();
                        Location freezeLoc = tempFrozenPlayer.getFreezeLoc();
                        if (freezeLoc.getBlockX() == originalLoc.getBlockX() && freezeLoc.getBlockZ() == originalLoc.getBlockZ() && originalLoc.getY() - freezeLoc.getY() > 3) {
                            playerManager.addFallingPlayer(uuid);
                        }

                        if (originalLoc != null && plugin.getConfig().getBoolean("tp-back")) {
                            p.teleport(originalLoc);
                        }

                        p.setAllowFlight(false);
                        p.setFlying(false);

                    } else {

                        Location originalLoc = SFLocation.fromString(plugin.getPlayerConfig().getConfig().getString("players." + uuid.toString() + ".original-location"));
                        Location freezeLoc = SFLocation.fromString(plugin.getPlayerConfig().getConfig().getString("players." + uuid.toString() + ".freeze-location"));
                        if (freezeLoc.getBlockX() == originalLoc.getBlockX() && freezeLoc.getBlockZ() == originalLoc.getBlockZ() && (originalLoc.getY() - freezeLoc.getY() > 3 || freezeLoc.getY() - locationManager.getGroundLocation(freezeLoc).getY() > 3)) {
                            List<String> fallingList = plugin.getPlayerConfig().getConfig().getStringList("falling-players");
                            fallingList.add(uuid.toString());
                            plugin.getPlayerConfig().getConfig().set("falling-players", fallingList);
                        }
                    }

                    plugin.getPlayerConfig().getConfig().set("players." + uuid.toString(), null);
                    plugin.getPlayerConfig().saveConfig();
                    plugin.getPlayerConfig().reloadConfig();
                    plugin.getPlayerManager().removeFrozenPlayer(uuid);

                    plugin.getMessageManager().removePlayer(p);

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
