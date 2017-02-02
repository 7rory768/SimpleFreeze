package org.plugins.simplefreeze.listeners;

import org.bukkit.Bukkit;
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
import org.plugins.simplefreeze.objects.FreezeAllPlayer;
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
    private final DataConverter dataConverter;

    public PlayerJoinListener(SimpleFreezeMain plugin, FreezeManager freezeManager, PlayerManager playerManager, LocationManager locationManager, HelmetManager helmetManager, DataConverter dataConverter) {
        this.plugin = plugin;
        this.freezeManager = freezeManager;
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        this.helmetManager = helmetManager;
        this.dataConverter = dataConverter;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        if (p.hasPermission("sf.notify.update") && !UpdateNotifier.getLatestVersion().equals(UpdateNotifier.getCurrentVersion())) {
            new BukkitRunnable() {

                @Override
                public void run() {
                    p.sendMessage(plugin.placeholders("{PREFIX}You are still running version &b" + UpdateNotifier.getCurrentVersion() + "\n{PREFIX}Latest version: &b" + UpdateNotifier.getLatestVersion()));

                }

            }.runTaskLater(this.plugin, 25L);
        }

        final String uuidStr = p.getUniqueId().toString();
        FrozenPlayer frozenPlayer = null;

        if (DataConverter.hasDataToConvert(p)) {
            frozenPlayer = this.dataConverter.convertData(p);
            if (frozenPlayer != null) {
                this.playerManager.addFrozenPlayer(p.getUniqueId(), frozenPlayer);
            }
        } else if (this.plugin.getPlayerConfig().getConfig().isSet("players." + uuidStr)) {
            Long freezeDate = this.plugin.getPlayerConfig().getConfig().getLong("players." + uuidStr + ".freeze-date");
            UUID freezerUUID = this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freezer-uuid").equals("null") ? null : UUID.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freezer-uuid"));
            String originalLocStr = this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".original-location");
            Location originalLocation = originalLocStr.equals("null") ? p.getLocation() : SFLocation.fromString(originalLocStr);
            if (originalLocation.equals(p.getLocation())) {
                this.plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".original-location", new SFLocation(p.getLocation()).toString());
                this.plugin.getPlayerConfig().saveConfig();
                this.plugin.getPlayerConfig().reloadConfig();
            }
            Location freezeLocation = SFLocation.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freeze-location"));
            boolean sqlFreeze = this.plugin.getPlayerConfig().getConfig().getBoolean("players." + uuidStr + ".mysql");
            if (this.plugin.getPlayerConfig().getConfig().isSet("players." + uuidStr + ".unfreeze-date")) {
                Long unfreezeDate = this.plugin.getPlayerConfig().getConfig().getLong("players." + uuidStr + ".unfreeze-date");
                if (System.currentTimeMillis() < unfreezeDate) {
                    frozenPlayer = new TempFrozenPlayer(freezeDate, unfreezeDate, p.getUniqueId(), freezerUUID, originalLocation, freezeLocation, sqlFreeze);
                    ((TempFrozenPlayer) frozenPlayer).startTask(plugin);
                    this.playerManager.addFrozenPlayer(p.getUniqueId(), frozenPlayer);
                } else if (!plugin.getPlayerConfig().getConfig().getBoolean("players." + uuidStr + ".mysql", false)) {
                    plugin.getPlayerConfig().getConfig().set("players." + uuidStr, null);
                    this.plugin.getPlayerConfig().saveConfig();
                    this.plugin.getPlayerConfig().reloadConfig();
                } else {
                    // SQL TABLE STUFF
                }
            } else {
                frozenPlayer = new FrozenPlayer(freezeDate, p.getUniqueId(), freezerUUID, originalLocation, freezeLocation, sqlFreeze);
                this.playerManager.addFrozenPlayer(p.getUniqueId(), frozenPlayer);
            }
        }

        if (frozenPlayer == null && this.freezeManager.freezeAllActive() && !(p.hasPermission("sf.exempt.*") || p.hasPermission("sf.exempt.freezeall"))) {
            UUID freezerUUID = this.plugin.getPlayerConfig().getConfig().getString("freezeall-info.freezer").equals("null") ? null : UUID.fromString(this.plugin.getPlayerConfig().getConfig().getString("freezeall-info.freezer"));

            SFLocation freezeLocation = this.plugin.getPlayerConfig().getConfig().getString("freezeall-info.location").equals("null") ? null : SFLocation.fromString(this.plugin.getPlayerConfig().getConfig().getString("freezeall-info.location"));
            if (freezeLocation == null && plugin.getPlayerConfig().getConfig().isSet("freezeall-info.players." + uuidStr + ".freeze-location")) {
                freezeLocation = plugin.getPlayerConfig().getConfig().getString("freezeall-info.players." + uuidStr + ".freeze-location").equals("null") ? null : SFLocation.fromString(plugin.getPlayerConfig().getConfig().getString("freezeall-info.players." + uuidStr + ".freeze-location"));
            } else if (freezeLocation == null) {
                if (this.plugin.getConfig().getBoolean("teleport-up")) {
                    freezeLocation = this.locationManager.getHighestAirLocation(new SFLocation(p.getLocation().clone()));
                } else {
                    freezeLocation = new SFLocation(new SFLocation(p.getLocation().clone()));
                    if (this.plugin.getConfig().getBoolean("enable-fly")) {
                        p.setAllowFlight(true);
                        p.setFlying(true);
                    }
                }

            }

            Long freezeDate = this.plugin.getPlayerConfig().getConfig().getLong("freezeall-info.date");

            frozenPlayer = new FreezeAllPlayer(freezeDate, p.getUniqueId(), freezerUUID, p.getLocation(), freezeLocation);
            this.playerManager.addFrozenPlayer(p.getUniqueId(), frozenPlayer);

            if (!this.plugin.getPlayerConfig().getConfig().isSet("freezeall-info.players." + uuidStr)) {
                this.plugin.getPlayerConfig().getConfig().set("freezeall-info.players." + uuidStr + ".original-location", new SFLocation(frozenPlayer.getOriginalLoc()).toString());
                this.plugin.getPlayerConfig().getConfig().set("freezeall-info.players." + uuidStr + ".freeze-location", freezeLocation == null ? "null" : freezeLocation.toString());
                this.plugin.getPlayerConfig().saveConfig();
                this.plugin.getPlayerConfig().reloadConfig();
            }

            final FrozenPlayer finalFreezeAllPlayer = frozenPlayer;

            new BukkitRunnable() {

                @Override
                public void run() {
                    finalFreezeAllPlayer.setHelmet(p.getInventory().getHelmet());
                    p.getInventory().setHelmet(helmetManager.getPersonalHelmetItem(finalFreezeAllPlayer));

                    if (finalFreezeAllPlayer.getFreezeLoc() == null) {
                        SFLocation originalLoc = new SFLocation(finalFreezeAllPlayer.getOriginalLoc());
                        Location freezeLoc;
                        if (plugin.getConfig().getBoolean("teleport-up")) {
                            freezeLoc = locationManager.getHighestAirLocation(originalLoc);
                        } else {
                            freezeLoc = new SFLocation(originalLoc.clone());
                            if (plugin.getConfig().getBoolean("enable-fly")) {
                                p.setAllowFlight(true);
                                p.setFlying(true);
                            }
                        }
                        finalFreezeAllPlayer.setFreezeLoc(freezeLoc);

                        if (plugin.getPlayerConfig().getConfig().getString("freezeall-info.players." + uuidStr + ".freeze-location").equals("null")) {
                            plugin.getPlayerConfig().getConfig().set("freezeall-info.players." + uuidStr + ".freeze-location", new SFLocation(freezeLoc).toString());
                        }
                    }

                    p.teleport(finalFreezeAllPlayer.getFreezeLoc());

                    String freezerName = "";
                    if (freezerUUID != null) {
                        freezerName = Bukkit.getPlayer(freezerUUID) == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer(freezerUUID).getName();
                    }

                    for (String msg : plugin.getConfig().getStringList("join-on-freezeall-message")) {
                        p.sendMessage(plugin.placeholders(msg.replace("{FREEZER}", freezerName)));
                    }
                }
            }.runTaskLater(this.plugin, 10L);

        } else if (frozenPlayer != null) {
            final FrozenPlayer finalFrozenPlayer = frozenPlayer;
            new BukkitRunnable() {

                @Override
                public void run() {
                    finalFrozenPlayer.setHelmet(p.getInventory().getHelmet());
                    p.getInventory().setHelmet(helmetManager.getPersonalHelmetItem(finalFrozenPlayer));

                    if (plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".original-location").equals("null")) {
                        finalFrozenPlayer.setOriginalLoc(p.getLocation());
                        plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".original-location", new SFLocation(p.getLocation()).toString());
                    }

                    if (finalFrozenPlayer.getFreezeLoc() == null) {
                        SFLocation originalLoc = new SFLocation(finalFrozenPlayer.getOriginalLoc());
                        Location freezeLoc;
                        if (plugin.getConfig().getBoolean("teleport-up")) {
                            freezeLoc = locationManager.getHighestAirLocation(originalLoc);
                        } else {
                            freezeLoc = new SFLocation(originalLoc.clone());
                            if (plugin.getConfig().getBoolean("enable-fly")) {
                                p.setAllowFlight(true);
                                p.setFlying(true);
                            }
                        }
                        finalFrozenPlayer.setFreezeLoc(freezeLoc);

                        if (plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freeze-location").equals("null")) {
                            plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".freeze-location", new SFLocation(freezeLoc).toString());
                        }
                    }
                    p.teleport(finalFrozenPlayer.getFreezeLoc());

                    if (plugin.getPlayerConfig().getConfig().getBoolean("players. " + uuidStr + ".message", false)) {
                        String location = locationManager.getLocationName(finalFrozenPlayer.getFreezeLoc());
                        String freezerName = finalFrozenPlayer.getFreezerName();
                        String timePlaceholder = "";
                        String serversPlaceholder = "";
                        String locationPlaceholder = location == null ? plugin.getConfig().getString("location") : plugin.getConfig().getString("locations." + location + ".placeholder", location);
                        String path;
                        if (finalFrozenPlayer instanceof TempFrozenPlayer) {
                            timePlaceholder = TimeUtil.formatTime((((TempFrozenPlayer) finalFrozenPlayer).getUnfreezeDate() - System.currentTimeMillis()) / 1000L);
                            path = "first-join.temp-frozen";
                        } else {
                            path = "first-join.frozen";
                        }

                        if (location != null) {
                            path += "-location";
                        }
                        p.sendMessage(plugin.placeholders(plugin.getConfig().getString(path).replace("{PLAYER}", p.getName()).replace("{FREEZER}", freezerName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder)));
                        plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".message", null);
                        plugin.getPlayerConfig().saveConfig();
                        plugin.getPlayerConfig().reloadConfig();
                    } else {
                        p.sendMessage(plugin.placeholders("{PREFIX}SimpleFreeze was re-enabled so you are now frozen again"));
                    }
                }
            }.runTaskLater(this.plugin, 10L);
        }
    }

}
