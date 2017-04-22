package org.plugins.simplefreeze.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.*;
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
    private final SoundManager soundManager;
    private final MessageManager messageManager;

    public PlayerJoinListener(SimpleFreezeMain plugin, FreezeManager freezeManager, PlayerManager playerManager, LocationManager locationManager, HelmetManager helmetManager, DataConverter dataConverter, SoundManager soundManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.freezeManager = freezeManager;
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        this.helmetManager = helmetManager;
        this.dataConverter = dataConverter;
        this.soundManager = soundManager;
        this.messageManager = messageManager;
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
            Location freezeLocation = SFLocation.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freeze-location"));
            if (freezeLocation == null && this.plugin.getConfig().getBoolean("teleport-to-ground")) {
                freezeLocation = new SFLocation(this.locationManager.getGroundLocation(p.getLocation()));
            } else if (freezeLocation == null) {
                freezeLocation = new SFLocation(p.getLocation().clone());
            }
            Location originalLocation = SFLocation.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".original-location"));
            if (freezeLocation.equals(originalLocation)) {
                freezeLocation = p.getLocation();
                originalLocation = p.getLocation();
            } else {
                originalLocation = p.getLocation();
            }
            String reason = this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".reason");
            if (this.plugin.getPlayerConfig().getConfig().isSet("players." + uuidStr + ".unfreeze-date")) {
                Long unfreezeDate = this.plugin.getPlayerConfig().getConfig().getLong("players." + uuidStr + ".unfreeze-date");
                if (System.currentTimeMillis() < unfreezeDate) {
                    frozenPlayer = new TempFrozenPlayer(freezeDate, unfreezeDate, p.getUniqueId(), freezerUUID, originalLocation, freezeLocation, reason, this.playerManager.isSQLFrozen(p));
                    ((TempFrozenPlayer) frozenPlayer).startTask(plugin);
                    this.playerManager.addFrozenPlayer(p.getUniqueId(), frozenPlayer);
                } else {
                    plugin.getPlayerConfig().getConfig().set("players." + uuidStr, null);
                    this.plugin.getPlayerConfig().saveConfig();
                    this.plugin.getPlayerConfig().reloadConfig();
                }
            } else {
                frozenPlayer = new FrozenPlayer(freezeDate, p.getUniqueId(), freezerUUID, originalLocation, freezeLocation, reason, this.playerManager.isSQLFrozen(p));
                this.playerManager.addFrozenPlayer(p.getUniqueId(), frozenPlayer);
            }
        }

        // if freezeall and player isnt already frozen
        if (frozenPlayer == null && this.freezeManager.freezeAllActive() && !(p.hasPermission("sf.exempt.*") || p.hasPermission("sf.exempt.freezeall"))) {
            final UUID freezerUUID = this.plugin.getPlayerConfig().getConfig().getString("freezeall-info.freezer").equals("null") ? null : UUID.fromString(this.plugin.getPlayerConfig().getConfig().getString("freezeall-info.freezer"));

            SFLocation freezeLocation = this.plugin.getPlayerConfig().getConfig().getString("freezeall-info.location").equals("null") ? null : SFLocation.fromString(this.plugin.getPlayerConfig().getConfig().getString("freezeall-info.location"));
            if (freezeLocation == null && this.plugin.getPlayerConfig().getConfig().isSet("freezeall-info.players." + uuidStr + ".freeze-location")) {
                freezeLocation = this.plugin.getPlayerConfig().getConfig().getString("freezeall-info.players." + uuidStr + ".freeze-location").equals("null") ? null : SFLocation.fromString(this.plugin.getPlayerConfig().getConfig().getString("freezeall-info.players." + uuidStr + ".freeze-location"));
            } else if (freezeLocation == null) {
                if (this.plugin.getConfig().getBoolean("teleport-to-ground")) {
                    freezeLocation = new SFLocation(this.locationManager.getGroundLocation(p.getLocation()));
                } else {
                    freezeLocation = new SFLocation(new SFLocation(p.getLocation().clone()));
                    if (this.plugin.getConfig().getBoolean("enable-fly")) {
                        p.setAllowFlight(true);
                        p.setFlying(true);
                    }
                }
            }
            Location originalLocation = SFLocation.fromString(this.plugin.getPlayerConfig().getConfig().getString("freezeall-info.players." + uuidStr + ".original-location"));
            if (freezeLocation.equals(originalLocation)) {
                freezeLocation = new SFLocation(p.getLocation());
                originalLocation = p.getLocation();
            } else {
                originalLocation = p.getLocation();
            }

            Long freezeDate = this.plugin.getPlayerConfig().getConfig().getLong("freezeall-info.date");
            String reason = this.plugin.getPlayerConfig().getConfig().getString("freezeall-info.reason");

            frozenPlayer = new FreezeAllPlayer(freezeDate, p.getUniqueId(), freezerUUID, originalLocation, freezeLocation, reason);
            this.playerManager.addFrozenPlayer(p.getUniqueId(), frozenPlayer);

            this.plugin.getPlayerConfig().getConfig().set("freezeall-info.players." + uuidStr + ".original-location", new SFLocation(frozenPlayer.getOriginalLoc()).toString());
            this.plugin.getPlayerConfig().getConfig().set("freezeall-info.players." + uuidStr + ".freeze-location", freezeLocation == null ? "null" : freezeLocation.toString());
            this.plugin.getPlayerConfig().saveConfig();
            this.plugin.getPlayerConfig().reloadConfig();

            final FrozenPlayer finalFreezeAllPlayer = frozenPlayer;

            new BukkitRunnable() {

                @Override
                public void run() {
                    finalFreezeAllPlayer.setHelmet(p.getInventory().getHelmet());
                    p.getInventory().setHelmet(helmetManager.getPersonalHelmetItem(finalFreezeAllPlayer));

                    if (finalFreezeAllPlayer.getFreezeLoc() == null) {
                        SFLocation originalLoc = new SFLocation(finalFreezeAllPlayer.getOriginalLoc());
                        Location freezeLoc;
                        if (plugin.getConfig().getBoolean("teleport-to-ground")) {
                            freezeLoc = locationManager.getGroundLocation(originalLoc);
                        } else {
                            freezeLoc = new SFLocation(originalLoc.clone());
                        }
                        finalFreezeAllPlayer.setFreezeLoc(freezeLoc);

                        if (plugin.getPlayerConfig().getConfig().getString("freezeall-info.players." + uuidStr + ".freeze-location").equals("null")) {
                            plugin.getPlayerConfig().getConfig().set("freezeall-info.players." + uuidStr + ".freeze-location", new SFLocation(freezeLoc).toString());
                        }
                    }

                    if (plugin.getConfig().getBoolean("enable-fly")) {
                        p.setAllowFlight(true);
                        p.setFlying(true);
                    }
                    p.teleport(finalFreezeAllPlayer.getFreezeLoc());

                    String freezerName = freezerUUID == null ? "CONSOLE" : Bukkit.getPlayer(freezerUUID) == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer(freezerUUID).getName();

                    for (String msg : plugin.getConfig().getStringList("join-on-freezeall-message")) {
                        if (msg == "") {
                            msg = " ";
                        }
                        p.sendMessage(plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{REASON}", reason)));
                    }

                    String totalMsg = "";
                    String location = locationManager.getLocationName(finalFreezeAllPlayer.getFreezeLoc());
                    String locPlaceholder = locationManager.getLocationPlaceholder(location);
                    if (location != null) {
                        for (String msg : plugin.getConfig().getStringList("freezeall-location-message")) {
                            if (msg.equals("")) {
                                msg = " ";
                            }
                            totalMsg += msg + "\n";
                        }
                        if (totalMsg.length() > 0) {
                            totalMsg = totalMsg.substring(0, totalMsg.length() - 2);
                        }
                        totalMsg = plugin.placeholders(totalMsg.replace("{LOCATION}", locPlaceholder).replace("{FREEZER}", finalFreezeAllPlayer.getFreezerName()).replace("{REASON}", reason));

                        if (messageManager.getFreezeAllLocInterval() > 0) {
                            messageManager.addFreezeAllLocPlayer(p, totalMsg);
                        }
                    } else {
                        for (String msg : plugin.getConfig().getStringList("freezeall-message")) {
                            if (msg.equals("")) {
                                msg = " ";
                            }
                            totalMsg += msg + "\n";
                        }
                        if (totalMsg.length() > 0) {
                            totalMsg = totalMsg.substring(0, totalMsg.length() - 2);
                        }
                        totalMsg = plugin.placeholders(totalMsg.replace("{LOCATION}", locPlaceholder).replace("{FREEZER}", finalFreezeAllPlayer.getFreezerName()).replace("{REASON}", reason));

                        if (messageManager.getFreezeAllInterval() > 0) {
                            messageManager.addFreezeAllPlayer(p, totalMsg);
                        }
                    }

                    soundManager.playFreezeSound(p);
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
                        if (plugin.getConfig().getBoolean("teleport-to-ground")) {
                            freezeLoc = locationManager.getGroundLocation(originalLoc);
                        } else {
                            freezeLoc = new SFLocation(originalLoc.clone());
                        }
                        finalFrozenPlayer.setFreezeLoc(freezeLoc);

                        if (plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freeze-location").equals("null")) {
                            plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".freeze-location", new SFLocation(freezeLoc).toString());
                        }
                    }

                    if (plugin.getConfig().getBoolean("enable-fly")) {
                        p.setAllowFlight(true);
                        p.setFlying(true);
                    }
                    p.teleport(finalFrozenPlayer.getFreezeLoc());

                    soundManager.playFreezeSound(p);

                    String locationName = locationManager.getLocationName(finalFrozenPlayer.getFreezeLoc());
                    String freezerName = finalFrozenPlayer.getFreezerName();
                    String timePlaceholder = "Permanent";
                    String serversPlaceholder = plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".servers", "");
                    String locationPlaceholder = locationManager.getLocationPlaceholder(locationName);
                    String reason = finalFrozenPlayer.getReason() == null ? plugin.getConfig().getString("default-reason") : finalFrozenPlayer.getReason();
                    if (plugin.getPlayerConfig().getConfig().getBoolean("players." + uuidStr + ".message", false)) {
                        String path;
                        if (finalFrozenPlayer instanceof TempFrozenPlayer) {
                            timePlaceholder = TimeUtil.formatTime((((TempFrozenPlayer) finalFrozenPlayer).getUnfreezeDate() - System.currentTimeMillis()) / 1000L);
                            path = "first-join.temp-frozen";
                        } else {
                            path = "first-join.frozen";
                        }

                        if (locationName != null) {
                            path += "-location";
                        }
                        p.sendMessage(plugin.placeholders(plugin.getConfig().getString(path).replace("{PLAYER}", p.getName()).replace("{FREEZER}", freezerName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{REASON}", reason)));
                        plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".message", null);
                        plugin.getPlayerConfig().saveConfig();
                        plugin.getPlayerConfig().reloadConfig();
                    } else {
                        for (String line : plugin.getConfig().getStringList("still-frozen-join")) {
                            p.sendMessage(plugin.placeholders(line.replace("{PLAYER}", p.getName()).replace("{FREEZER}", freezerName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{REASON}", reason).replace("{SERVERS}", serversPlaceholder)));
                        }
                    }

                    String path = "";
                    if (finalFrozenPlayer instanceof TempFrozenPlayer) {
                        path = "temp-freeze-message";
                        if (locationName != null) {
                            path = "temp-freeze-location-message";
                        }
                    } else {
                        path = "freeze-message";
                        if (locationName != null) {
                            path = "freeze-location-message";
                        }
                    }

                    if (!serversPlaceholder.equals("")) {
                        path = "sql-" + path;
                    }

                    String msg = "";
                    for (String line : plugin.getConfig().getStringList(path)) {
                        if (line.equals("")) {
                            line = " ";
                        }
                        msg += line + "\n";
                    }
                    msg = msg.length() > 2 ? msg.substring(0, msg.length() - 1) : "";
                    msg = msg.replace("{PLAYER}", p.getName()).replace("{TIME}", timePlaceholder).replace("{FREEZER}", finalFrozenPlayer.getFreezerName()).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reason);

                    if (finalFrozenPlayer instanceof TempFrozenPlayer) {
                        if (locationName == null && messageManager.getTempFreezeInterval() > 0) {
                            messageManager.addTempFreezePlayer(p, msg);
                        } else if (messageManager.getTempFreezeLocInterval() > 0) {
                            messageManager.addTempFreezeLocPlayer(p, msg);
                        }
                    } else {
                        if (locationName == null && messageManager.getFreezeInterval() > 0) {
                            messageManager.addFreezePlayer(p, msg);
                        } else if (messageManager.getFreezeLocInterval() > 0) {
                            messageManager.addFreezeLocPlayer(p, msg);
                        }
                    }
                }
            }.runTaskLater(this.plugin, 10L);
        }
    }

}
