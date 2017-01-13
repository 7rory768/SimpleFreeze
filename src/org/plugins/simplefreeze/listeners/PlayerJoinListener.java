package org.plugins.simplefreeze.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.FreezeManager;
import org.plugins.simplefreeze.managers.HelmetManager;
import org.plugins.simplefreeze.managers.LocationManager;
import org.plugins.simplefreeze.managers.PlayerManager;
import org.plugins.simplefreeze.objects.FrozenPlayer;
import org.plugins.simplefreeze.objects.SFLocation;
import org.plugins.simplefreeze.objects.TempFrozenPlayer;
import org.plugins.simplefreeze.util.DataConverter;
import org.plugins.simplefreeze.util.TimeUtil;
import org.plugins.simplefreeze.util.UpdateNotifier;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final SimpleFreezeMain plugin;
    private final FreezeManager freezeManager;
    private final PlayerManager playerManager;
    private final LocationManager locationManager;
    private final HelmetManager helmetManager;

    public PlayerJoinListener(SimpleFreezeMain plugin, FreezeManager freezeManager, PlayerManager playerManager, LocationManager locationManager, HelmetManager helmetManager) {
        this.plugin = plugin;
        this.freezeManager = freezeManager;
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        this.helmetManager = helmetManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        final String uuidStr = p.getUniqueId().toString();
        if (p.hasPermission("sf.notify.update") && !UpdateNotifier.getLatestVersion().equals(UpdateNotifier.getCurrentVersion())) {
            new BukkitRunnable() {

                @Override
                public void run() {
                    p.sendMessage(plugin.placeholders("{PREFIX}You are still running version &b" + UpdateNotifier.getCurrentVersion() + "\n{PREFIX}Latest version: &b" + UpdateNotifier.getLatestVersion()));

                }

            }.runTaskLater(this.plugin, 25L);
        }
        final FrozenPlayer frozenPlayer;
        if (DataConverter.hasDataToConvert(p)) {
            frozenPlayer = DataConverter.convertData(p);
            long freezeDate = System.currentTimeMillis();
            this.plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".freeze-date", freezeDate);
            this.plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".freezer-uuid", "null");
            this.plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".original-location", new SFLocation(frozenPlayer.getOriginalLoc()).toString());
            this.plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".freeze-location", new SFLocation(p.getLocation()).toString());
            this.plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".mysql", false);
            this.plugin.getPlayerConfig().saveConfig();
            this.plugin.getPlayerConfig().reloadConfig();
            DataConverter.removeData(p);
            this.playerManager.addFrozenPlayer(p.getUniqueId(), frozenPlayer);
        } else if (this.plugin.getPlayerConfig().getConfig().isSet("players." + uuidStr)) {
            Long freezeDate = this.plugin.getPlayerConfig().getConfig().getLong("players." + uuidStr + ".freeze-date");
            UUID freezerUUID = this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freezer-uuid").equals("null") ? null : UUID.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freezer-uuid"));
            Location originalLocation = SFLocation.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".original-location"));
            Location freezeLocation = SFLocation.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freeze-location"));
            boolean sqlFreeze = this.plugin.getPlayerConfig().getConfig().getBoolean("players." + uuidStr + ".mysql");
            if (this.plugin.getPlayerConfig().getConfig().isSet("players." + uuidStr + ".unfreeze-date")) {
                Long unfreezeDate = this.plugin.getPlayerConfig().getConfig().getLong("players." + uuidStr + ".freeze-date");
                if (System.currentTimeMillis() < unfreezeDate) {
                    frozenPlayer = new TempFrozenPlayer(freezeDate, unfreezeDate, p.getUniqueId(), freezerUUID, originalLocation, freezeLocation, sqlFreeze);
                    ((TempFrozenPlayer) frozenPlayer).startTask(this.plugin);
                    this.playerManager.addFrozenPlayer(p.getUniqueId(), frozenPlayer);
                } else if (!this.plugin.getPlayerConfig().getConfig().getBoolean("players." + uuidStr + ".mysql", false)) {
                    frozenPlayer = null;
                    plugin.getPlayerConfig().getConfig().set("players." + uuidStr, null);
                    plugin.getPlayerConfig().saveConfig();
                    plugin.getPlayerConfig().reloadConfig();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (p != null) {
                                // ERROR HERE
                                p.teleport(SFLocation.fromString(plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".original-location")));
                            }
                        }
                    }.runTaskLater(plugin, 1L);
                } else {
                    frozenPlayer = null;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            //SQL TABLE STUFF
                        }
                    }.runTaskLater(plugin, 1L);
                }
            } else {
                frozenPlayer = new FrozenPlayer(freezeDate, p.getUniqueId(), freezerUUID, originalLocation, freezeLocation, sqlFreeze);
                this.playerManager.addFrozenPlayer(p.getUniqueId(), frozenPlayer);
            }
        } else {
            frozenPlayer = null;
        }

        if (this.playerManager.isFrozen(p)) {
            new BukkitRunnable() {

                @Override
                public void run() {
                    frozenPlayer.setHelmet(p.getInventory().getHelmet());
                    p.getInventory().setHelmet(helmetManager.getPersonalHelmetItem(frozenPlayer));
                    if (plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".original-location").equals("null")) {
                        frozenPlayer.setOriginalLoc(p.getLocation());
                    }

                    if (plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freeze-location").equals("null")) {
                        SFLocation originalLoc = new SFLocation(frozenPlayer.getOriginalLoc());
                        Location freezeLoc = null;
                        if (freezeLoc == null && plugin.getConfig().getBoolean("teleport-up")) {
                            freezeLoc = locationManager.getHighestAirLocation(originalLoc);
                        } else if (freezeLoc == null) {
                            freezeLoc = new SFLocation(originalLoc.clone());
                            if (plugin.getConfig().getBoolean("enable-fly")) {
                                p.setAllowFlight(true);
                                p.setFlying(true);
                            }
                        }
                        frozenPlayer.setFreezeLoc(freezeLoc);
                    }
                    p.teleport(frozenPlayer.getFreezeLoc());
                    if (plugin.getPlayerConfig().getConfig().getBoolean("players." + uuidStr + ".message", false)) {
                        String location = locationManager.getLocationName(frozenPlayer.getFreezeLoc());
                        String freezerName = frozenPlayer.getFreezerName();
                        String timePlaceholder = "";
                        String serversPlaceholder = "";
                        String locationPlaceholder = location == null ? plugin.getConfig().getString("location") : plugin.getConfig().getString("locations." + location + ".placeholder", location);
                        String path;
                        if (frozenPlayer instanceof TempFrozenPlayer) {
                            timePlaceholder = TimeUtil.formatTime((((TempFrozenPlayer) frozenPlayer).getUnfreezeDate() - System.currentTimeMillis()) / 1000L);
                            path = "first-join.temp-frozen";
                            if (location != null) {
                                path = "first-join.temp-frozen-location";
                            }
                        } else {
                            path = "first-join.frozen";
                            if (location != null) {
                                path = "first-join.frozen-location";
                            }
                        }
                        p.sendMessage(plugin.placeholders(plugin.getConfig().getString(path).replace("{PLAYER}", p.getName()).replace("{FREEZER}", freezerName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder)));
                        plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".message", null);
                        plugin.getPlayerConfig().saveConfig();
                        plugin.getPlayerConfig().reloadConfig();
                    }
                }
            }.runTaskLater(this.plugin, 10L);
        }
    }

}
