package org.plugins.simplefreeze.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.cache.FrozenPages;
import org.plugins.simplefreeze.objects.FreezeAllPlayer;
import org.plugins.simplefreeze.objects.FrozenPlayer;
import org.plugins.simplefreeze.objects.SFLocation;
import org.plugins.simplefreeze.objects.TempFrozenPlayer;
import org.plugins.simplefreeze.util.TimeUtil;

import java.util.List;
import java.util.UUID;

public class FreezeManager {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;
    private final HelmetManager helmetManager;
    private final LocationManager locationManager;
    private final FrozenPages frozenPages;
    private final SoundManager soundManager;
    private final MessageManager messageManager;

    private boolean freezeAll = false;

    public FreezeManager(SimpleFreezeMain plugin, PlayerManager playerManager, HelmetManager helmetManager, LocationManager locationManager, FrozenPages frozenPages, SoundManager soundManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.helmetManager = helmetManager;
        this.locationManager = locationManager;
        this.frozenPages = frozenPages;
        this.freezeAll = this.plugin.getPlayerConfig().getConfig().getBoolean("freezeall-info.freezeall");
        this.soundManager = soundManager;
        this.messageManager = messageManager;
    }

    public boolean freezeAllActive() {
        return this.freezeAll;
    }

    public void unfreeze(UUID uuid) {
        if (this.playerManager.isFrozen(uuid)) {
            FrozenPlayer frozenPlayer = this.playerManager.getFrozenPlayer(uuid);
            if (frozenPlayer instanceof TempFrozenPlayer) {
                TempFrozenPlayer tempFrozenPlayer = (TempFrozenPlayer) frozenPlayer;
                tempFrozenPlayer.cancelTask();
            }
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                if (frozenPlayer.getHelmet() != null) {
                    p.getInventory().setHelmet(frozenPlayer.getHelmet());
                } else {
                    p.getInventory().setHelmet(null);
                }

                p.setAllowFlight(false);
                p.setFlying(false);
                if (frozenPlayer.getOriginalLoc() != null && this.plugin.getConfig().getBoolean("tp-back")) {
                    p.teleport(frozenPlayer.getOriginalLoc());
                }

                this.soundManager.playUnfreezeSound(p);

                Location originalLoc = frozenPlayer.getOriginalLoc();
                Location freezeLoc = frozenPlayer.getFreezeLoc();
                if (freezeLoc.getBlockX() == originalLoc.getBlockX() && freezeLoc.getBlockZ() == originalLoc.getBlockZ() && (originalLoc.getY() - freezeLoc.getY() > 3 || freezeLoc.getY() - this.locationManager.getGroundLocation(freezeLoc).getY() > 3)) {
                    this.playerManager.addFallingPlayer(uuid);
                }

                this.messageManager.removePlayer(p);

            } else {
                Location originalLoc = SFLocation.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + uuid.toString() + ".original-location"));
                Location freezeLoc = SFLocation.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + uuid.toString() + ".freeze-location"));
                if (freezeLoc != null && originalLoc != null && freezeLoc.getBlockX() == originalLoc.getBlockX() && freezeLoc.getBlockZ() == originalLoc.getBlockZ() && originalLoc.getY() - freezeLoc.getY() > 3) {
                    List<String> fallingList = this.plugin.getPlayerConfig().getConfig().getStringList("falling-players");
                    fallingList.add(uuid.toString());
                    this.plugin.getPlayerConfig().getConfig().set("falling-players", fallingList);
                }
            }

            this.plugin.getPlayerConfig().getConfig().set("players." + uuid.toString(), null);
            this.plugin.getPlayerConfig().getConfig().set("freezeall-info.players." + uuid.toString(), null);
            this.plugin.getPlayerConfig().saveConfig();
            this.plugin.getPlayerConfig().reloadConfig();
            this.playerManager.removeFrozenPlayer(uuid);

            this.plugin.getStatsConfig().getConfig().set("unfreeze-count", this.plugin.getStatsConfig().getConfig().getInt("unfreeze-count", 0) + 1);
            this.plugin.getStatsConfig().saveConfig();
            this.plugin.getStatsConfig().reloadConfig();
        }
    }

    public void unfreezeAll() {
        this.plugin.getPlayerConfig().getConfig().set("freezeall-info.freezeall", false);
        this.plugin.getPlayerConfig().getConfig().set("freezeall-info.freezer", "null");
        this.plugin.getPlayerConfig().getConfig().set("freezeall-info.location", "null");
        this.plugin.getPlayerConfig().getConfig().set("freezeall-info.date", "null");
        this.plugin.getPlayerConfig().getConfig().set("freezeall-info.reason", "");
        this.plugin.getPlayerConfig().saveConfig();
        this.plugin.getPlayerConfig().reloadConfig();
        this.freezeAll = false;
        for (String uuidStr : this.plugin.getPlayerConfig().getConfig().getConfigurationSection("freezeall-info.players").getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                if (this.playerManager.isFreezeAllFrozen(uuid)) {
                    FreezeAllPlayer freezeAllPlayer = (FreezeAllPlayer) this.playerManager.getFrozenPlayer(uuid);
                    p.getInventory().setHelmet(freezeAllPlayer.getHelmet());

                    p.setAllowFlight(false);
                    p.setFlying(false);
                    p.teleport(freezeAllPlayer.getOriginalLoc());

                    Location originalLoc = freezeAllPlayer.getOriginalLoc();
                    Location freezeLoc = freezeAllPlayer.getFreezeLoc();
                    if (freezeLoc.getBlockX() == originalLoc.getBlockX() && freezeLoc.getBlockZ() == originalLoc.getBlockZ() && originalLoc.getY() - freezeLoc.getY() > 3) {
                        this.playerManager.addFallingPlayer(uuid);
                    }

                    this.playerManager.removeFrozenPlayer(uuid);
                }

                this.soundManager.playUnfreezeSound(p);
            } else {
                Location originalLoc = SFLocation.fromString(this.plugin.getPlayerConfig().getConfig().getString("freezeall-info.players." + uuidStr + "original-location"));
                Location freezeLoc = SFLocation.fromString(this.plugin.getPlayerConfig().getConfig().getString("freezeall-info.players." + uuidStr + "freeze-location"));
                if (originalLoc != null && freezeLoc != null) {
                    if (freezeLoc.getBlockX() == originalLoc.getBlockX() && freezeLoc.getBlockZ() == originalLoc.getBlockZ() && (originalLoc.getY() - freezeLoc.getY() > 3 || freezeLoc.getY() - this.locationManager.getGroundLocation(freezeLoc).getY() > 3)) {
                        List<String> fallingList = this.plugin.getPlayerConfig().getConfig().getStringList("falling-players");
                        fallingList.add(uuid.toString());
                        this.plugin.getPlayerConfig().getConfig().set("falling-players", fallingList);
                    }
                }
            }
        }
        this.plugin.getPlayerConfig().getConfig().set("freezeall-info.players", null);
        this.plugin.getPlayerConfig().saveConfig();
        this.plugin.getPlayerConfig().reloadConfig();
        this.messageManager.clearFreezeAllPlayers();
    }

    public void freezeAll(UUID freezerUUID, String location, String reason) {
        this.freezeAll = true;
        this.plugin.getPlayerConfig().getConfig().set("freezeall-info.freezeall", true);
        this.plugin.getPlayerConfig().getConfig().set("freezeall-info.freezer", freezerUUID == null ? "null" : freezerUUID.toString());
        long freezeDate = System.currentTimeMillis();
        this.plugin.getPlayerConfig().getConfig().set("freezeall-info.date", freezeDate);
        if (reason != null) {
            this.plugin.getPlayerConfig().getConfig().set("freezeall-info.reason", reason);
        }

        SFLocation freezeLoc = null;

        if (location != null) {
            freezeLoc = this.locationManager.getSFLocation(location);
        }

        if (freezeLoc != null) {
            this.plugin.getPlayerConfig().getConfig().set("freezeall-info.location", freezeLoc.toString());
        }

        this.plugin.getPlayerConfig().saveConfig();
        this.plugin.getPlayerConfig().reloadConfig();

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (!this.playerManager.isFrozen(p.getUniqueId())) {
                if (!(p.hasPermission("sf.exempt.*") || p.hasPermission("sf.exempt.freezeall"))) {
                    UUID freezeeUUID = p.getUniqueId();
                    ItemStack helmet = null;
                    SFLocation originalLoc = null;

                    if (freezeLoc != null) {
                        if (!this.plugin.getConfig().isSet("locations." + location + ".yaw") && p != null) {
                            freezeLoc.setYaw(p.getLocation().getYaw());
                        }
                        if (!this.plugin.getConfig().isSet("locations." + location + ".pitch") && p != null) {
                            freezeLoc.setPitch(p.getLocation().getPitch());
                        }
                    }

                    if (p.getInventory().getHelmet() != null) {
                        helmet = p.getInventory().getHelmet();
                    }

                    originalLoc = new SFLocation(p.getLocation());
                    if (freezeLoc == null && this.plugin.getConfig().getBoolean("teleport-to-ground")) {
                        freezeLoc = new SFLocation(this.locationManager.getGroundLocation(originalLoc));
                    } else if (freezeLoc == null) {
                        freezeLoc = new SFLocation(originalLoc.clone());
                    }

                    if (this.plugin.getConfig().getBoolean("enable-fly")) {
                        p.setAllowFlight(true);
                        p.setFlying(true);
                    }
                    if (!freezeLoc.equals(originalLoc)) {
                        p.teleport(freezeLoc);
                    }

                    this.plugin.getPlayerConfig().getConfig().set("freezeall-info.players." + freezeeUUID.toString() + ".original-location", originalLoc == null ? "null" : originalLoc.toString());
                    this.plugin.getPlayerConfig().getConfig().set("freezeall-info.players." + freezeeUUID.toString() + ".freeze-location", freezeLoc == null ? "null" : freezeLoc.toString());
                    this.plugin.getPlayerConfig().saveConfig();
                    this.plugin.getPlayerConfig().reloadConfig();

                    if (p != null) {
                        FreezeAllPlayer freezeAllPlayer = new FreezeAllPlayer(freezeDate, freezeeUUID, freezerUUID, originalLoc, freezeLoc, reason, helmet);
                        this.playerManager.addFrozenPlayer(freezeeUUID, freezeAllPlayer);
                        p.getInventory().setHelmet(this.helmetManager.getPersonalHelmetItem(freezeAllPlayer));

                        this.soundManager.playFreezeSound(p);
                    }
                }
            }
            if (location != null) {
                freezeLoc = this.locationManager.getSFLocation(location);
            } else if (location == null) {
                freezeLoc = null;
            }
        }

        this.plugin.getStatsConfig().getConfig().set("freezeall-count", this.plugin.getStatsConfig().getConfig().getInt("freezeall-count", 0) + 1);
        this.plugin.getStatsConfig().saveConfig();
        this.plugin.getStatsConfig().reloadConfig();
    }

    public void freeze(UUID freezeeUUID, UUID freezerUUID, String location, String reason, String serversString) {
        if (!this.playerManager.isFrozen(freezeeUUID)) {
            ItemStack helmet = null;
            SFLocation originalLoc = null;
            SFLocation freezeLoc = null;
            Player onlineFreezee = Bukkit.getPlayer(freezeeUUID);
            String freezerName = freezerUUID == null ? "CONSOLE" : Bukkit.getPlayer(freezerUUID) == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer
                    (freezerUUID).getName();

            if (location != null) {
                freezeLoc = this.locationManager.getSFLocation(location);
                if (!this.plugin.getConfig().isSet("locNames." + location + ".yaw") && onlineFreezee != null) {
                    freezeLoc.setYaw(onlineFreezee.getLocation().getYaw());
                }
                if (!this.plugin.getConfig().isSet("locNames." + location + ".pitch") && onlineFreezee != null) {
                    freezeLoc.setPitch(onlineFreezee.getLocation().getPitch());
                }
            }

            if (onlineFreezee != null) {
                if (onlineFreezee.getInventory().getHelmet() != null) {
                    helmet = onlineFreezee.getInventory().getHelmet();
                }
                originalLoc = new SFLocation(onlineFreezee.getLocation());
                if (freezeLoc == null && this.plugin.getConfig().getBoolean("teleport-to-ground")) {
                    freezeLoc = new SFLocation(this.locationManager.getGroundLocation(originalLoc));
                } else if (freezeLoc == null) {
                    freezeLoc = new SFLocation(originalLoc.clone());
                }

                if (this.plugin.getConfig().getBoolean("enable-fly")) {
                    onlineFreezee.setAllowFlight(true);
                    onlineFreezee.setFlying(true);
                }
                if (!freezeLoc.equals(originalLoc)) {
                    onlineFreezee.teleport(freezeLoc);
                }

                this.soundManager.playFreezeSound(onlineFreezee);

            }

            long freezeDate = System.currentTimeMillis();
            this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freeze-date", freezeDate);
            this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freezer-uuid", freezerName.equals("CONSOLE") ? "null" : Bukkit.getPlayerExact(freezerName).getUniqueId().toString());
            this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".original-location", originalLoc == null ? "null" : originalLoc.toString());
            this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freeze-location", freezeLoc == null ? "null" : freezeLoc.toString());
            if (reason != null) {
                this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".reason", reason);
            }
            if (serversString != null) {
                this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".servers", serversString);
            }
            if (onlineFreezee == null) {
                this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".message", true);
            }
            this.plugin.getPlayerConfig().saveConfig();
            this.plugin.getPlayerConfig().reloadConfig();

            if (this.freezeAll == true) {
                this.plugin.getPlayerConfig().getConfig().set("freezeall-players." + freezeeUUID, null);
                this.plugin.getPlayerConfig().saveConfig();
                this.plugin.getPlayerConfig().reloadConfig();
            }

            if (onlineFreezee != null) {
                FrozenPlayer frozenPlayer = new FrozenPlayer(freezeDate, freezeeUUID, freezerUUID, originalLoc, freezeLoc, reason, serversString != null, helmet);
                this.playerManager.addFrozenPlayer(freezeeUUID, frozenPlayer);
                ItemStack personalHelmet = this.helmetManager.getPersonalHelmetItem(frozenPlayer);
                if (personalHelmet != null) {
                    onlineFreezee.getInventory().setHelmet(personalHelmet);
                }
            } else {
                this.frozenPages.refreshString(freezeeUUID);
            }

            if (this.plugin.usingMySQL()) {
                this.plugin.getSQLManager().addToFrozenList(freezeeUUID);
            }

            this.plugin.getStatsConfig().getConfig().set("freeze-count", this.plugin.getStatsConfig().getConfig().getInt("freeze-count", 0) + 1);
            this.plugin.getStatsConfig().saveConfig();
            this.plugin.getStatsConfig().reloadConfig();
        }
    }

    public void tempFreeze(final UUID freezeeUUID, UUID freezerUUID, String location, long time, String reason, String serversString) {
        if (!this.playerManager.isFrozen(freezeeUUID)) {
            ItemStack helmet = null;
            SFLocation originalLoc = null;
            SFLocation freezeLoc = null;
            final Player onlineFreezee = Bukkit.getPlayer(freezeeUUID);
            String freezerName = freezerUUID == null ? "CONSOLE" : Bukkit.getPlayer(freezerUUID) == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer
                    (freezerUUID).getName();

            if (location != null) {
                if (location != null) {
                    freezeLoc = this.locationManager.getSFLocation(location);
                    if (!this.plugin.getConfig().isSet("locNames." + location + ".yaw") && onlineFreezee != null) {
                        freezeLoc.setYaw(onlineFreezee.getLocation().getYaw());
                    }
                    if (!this.plugin.getConfig().isSet("locNames." + location + ".pitch") && onlineFreezee != null) {
                        freezeLoc.setPitch(onlineFreezee.getLocation().getPitch());
                    }
                }
            }

            if (onlineFreezee != null) {
                if (onlineFreezee.getInventory().getHelmet() != null) {
                    helmet = onlineFreezee.getInventory().getHelmet();
                }

                originalLoc = new SFLocation(onlineFreezee.getLocation());
                if (freezeLoc == null && this.plugin.getConfig().getBoolean("teleport-to-ground")) {
                    freezeLoc = new SFLocation(this.locationManager.getGroundLocation(originalLoc));
                } else if (freezeLoc == null) {
                    freezeLoc = new SFLocation(originalLoc.clone());
                }

                if (this.plugin.getConfig().getBoolean("enable-fly")) {
                    onlineFreezee.setAllowFlight(true);
                    onlineFreezee.setFlying(true);
                }
                if (!freezeLoc.equals(originalLoc)) {
                    onlineFreezee.teleport(freezeLoc);
                }

                this.soundManager.playFreezeSound(onlineFreezee);
            }

            long freezeDate = System.currentTimeMillis();
            long unfreezeDate = freezeDate + (time * 1000L);
            this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freeze-date", freezeDate);
            this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".unfreeze-date", unfreezeDate);
            this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freezer-uuid", freezerName.equals("CONSOLE") ? "null" : Bukkit.getPlayerExact(freezerName).getUniqueId().toString());
            this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".original-location", originalLoc == null ? "null" : originalLoc.toString());
            this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freeze-location", freezeLoc == null ? "null" : freezeLoc.toString());
            if (reason != null) {
                this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".reason", reason);
            }
            if (serversString != null) {
                this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".servers", serversString);
            }
            if (onlineFreezee == null) {
                this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".message", true);
            }
            this.plugin.getPlayerConfig().saveConfig();
            this.plugin.getPlayerConfig().reloadConfig();
            final TempFrozenPlayer tempFrozenPlayer = new TempFrozenPlayer(freezeDate, unfreezeDate, freezeeUUID, freezerUUID, originalLoc, freezeLoc, reason, serversString != null, helmet);
            if (onlineFreezee != null) {
                onlineFreezee.getInventory().setHelmet(this.helmetManager.getPersonalHelmetItem(tempFrozenPlayer));
            }

            if (this.freezeAll == true) {
                this.plugin.getPlayerConfig().getConfig().set("freezeall-players." + freezeeUUID, null);
                this.plugin.getPlayerConfig().saveConfig();
                this.plugin.getPlayerConfig().reloadConfig();
            }

            this.plugin.getStatsConfig().getConfig().set("temp-freeze-count", this.plugin.getStatsConfig().getConfig().getInt("temp-freeze-count", 0) + 1);
            this.plugin.getStatsConfig().saveConfig();
            this.plugin.getStatsConfig().reloadConfig();

            if (onlineFreezee != null) {
                tempFrozenPlayer.startTask(this.plugin);
                this.playerManager.addFrozenPlayer(freezeeUUID, tempFrozenPlayer);
            } else {
                this.frozenPages.refreshString(freezeeUUID);
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    frozenPages.removePlayer(tempFrozenPlayer.getFreezeeUUID());
                }
            }.runTaskLater(plugin, (tempFrozenPlayer.getUnfreezeDate() - System.currentTimeMillis()) / 1000L * 20L);
            if (this.plugin.usingMySQL()) {
                this.plugin.getSQLManager().addToFrozenList(freezeeUUID);
            }
        }
    }

    public void notifyOfFreeze(UUID freezeeUUID) {
        String location = this.locationManager.getLocationName(SFLocation.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + freezeeUUID.toString() + "" +
                ".freeze-location")));
        UUID freezerUUID;
        try {
            freezerUUID = UUID.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + freezeeUUID.toString() + ".freezer-uuid"));
        } catch (IllegalArgumentException e) {
            freezerUUID = null;
        }
        Player onlineFreezee = Bukkit.getPlayer(freezeeUUID);
        String freezeeName = Bukkit.getPlayer(freezeeUUID) == null ? Bukkit.getOfflinePlayer(freezeeUUID).getName() : Bukkit.getPlayer(freezeeUUID).getName();
        String freezerName = freezerUUID == null ? "CONSOLE" : Bukkit.getPlayer(freezerUUID) == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer(freezerUUID).getName();
        String timePlaceholder = "Permanent";
        String serversPlaceholder = "";
        String locationPlaceholder = this.locationManager.getLocationPlaceholder(location);
        String reason = this.plugin.getPlayerConfig().getConfig().getString("players." + freezeeUUID.toString() + ".reason", "");

        String playerPath;
        String notifyPath;

        if (this.plugin.getPlayerConfig().getConfig().isSet("players." + freezeeUUID.toString() + ".unfreeze-date")) {
            timePlaceholder = TimeUtil.formatTime((this.plugin.getPlayerConfig().getConfig().getLong("players." + freezeeUUID.toString() + ".unfreeze-date") - System.currentTimeMillis()) / 1000L);
            playerPath = "temp-freeze-message";
            if (location != null) {
                playerPath = "temp-freeze-location-message";
            }
            notifyPath = "temp-frozen-notify-message";
        } else {
            playerPath = "freeze-message";
            if (location != null) {
                playerPath = "freeze-location-message";
            }
            notifyPath = "frozen-notify-message";
        }

        if (onlineFreezee != null) {
            String totalMsg = "";
            for (String msg : this.plugin.getConfig().getStringList(playerPath)) {
                if (msg.equals("")) {
                    msg = " ";
                }
                totalMsg += msg + "\n";
                onlineFreezee.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reason)));
            }
            totalMsg = totalMsg.length() > 2 ? totalMsg.substring(0, totalMsg.length() - 1) : "";
            totalMsg = totalMsg.replace("{PLAYER}", freezeeName).replace("{FREEZER}", freezerName).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reason);
            if (playerPath.equalsIgnoreCase("temp-freeze-message") && this.messageManager.getTempFreezeInterval() > 0) {
                this.messageManager.addTempFreezePlayer(onlineFreezee, totalMsg);
            } else if (playerPath.equalsIgnoreCase("temp-freeze-location-message") && this.messageManager.getTempFreezeLocInterval() > 0) {
                this.messageManager.addTempFreezeLocPlayer(onlineFreezee, totalMsg);
            } else if (playerPath.equalsIgnoreCase("freeze-message") && this.messageManager.getFreezeInterval() > 0) {
                this.messageManager.addFreezePlayer(onlineFreezee, totalMsg);
            } else if (playerPath.equalsIgnoreCase("freeze-location-message") && this.messageManager.getFreezeLocInterval() > 0) {
                this.messageManager.addFreezeLocPlayer(onlineFreezee, totalMsg);
            }

        }

        Player freezerP = freezerUUID == null ? null : Bukkit.getPlayer(freezerUUID) == null ? null : Bukkit.getPlayer(freezerUUID);
        for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
            if (freezerP != null) {
                freezerP.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reason)));
            } else {
                Bukkit.getConsoleSender().sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reason)));
            }

        }

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.hasPermission("sf.notify.freeze") && p != freezerP) {
                for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
                    p.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}",
                            timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reason)));
                }
            }

        }
    }

    public void notifyOfFreeze(FrozenPlayer frozenPlayer) {
        String location = this.locationManager.getLocationName(frozenPlayer.getFreezeLoc());
        Player onlineFreezee = Bukkit.getPlayer(frozenPlayer.getFreezeeUUID());
        String freezeeName = frozenPlayer.getFreezeeName();
        String freezerName = frozenPlayer.getFreezerName();
        String timePlaceholder = "Permanent";
        String serversPlaceholder = "";
        String locationPlaceholder = this.locationManager.getLocationPlaceholder(location);
        String reason = this.plugin.getPlayerConfig().getConfig().getString("players." + frozenPlayer.getFreezeeUUID().toString() + ".reason", "");

        String playerPath;
        String notifyPath;
        if (frozenPlayer instanceof TempFrozenPlayer) {
            timePlaceholder = TimeUtil.formatTime((((TempFrozenPlayer) frozenPlayer).getUnfreezeDate() - System.currentTimeMillis()) / 1000L);
            playerPath = "temp-freeze-message";
            if (location != null) {
                playerPath = "temp-freeze-location-message";
            }
            notifyPath = "temp-frozen-notify-message";
        } else {
            playerPath = "freeze-message";
            if (location != null) {
                playerPath = "freeze-location-message";
            }
            notifyPath = "frozen-notify-message";
        }

        if (onlineFreezee != null) {
            String totalMsg = "";
            for (String msg : this.plugin.getConfig().getStringList(playerPath)) {
                if (msg.equals("")) {
                    msg = " ";
                }
                totalMsg += msg + "\n";
                onlineFreezee.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reason)));
            }
            totalMsg = totalMsg.length() > 2 ? totalMsg.substring(0, totalMsg.length() - 1) : "";
            totalMsg = totalMsg.replace("{PLAYER}", freezeeName).replace("{FREEZER}", freezerName).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reason);
            if (playerPath.equalsIgnoreCase("temp-freeze-message") && this.messageManager.getTempFreezeInterval() > 0) {
                this.messageManager.addTempFreezePlayer(onlineFreezee, totalMsg);
            } else if (playerPath.equalsIgnoreCase("temp-freeze-location-message") && this.messageManager.getTempFreezeLocInterval() > 0) {
                this.messageManager.addTempFreezeLocPlayer(onlineFreezee, totalMsg);
            } else if (playerPath.equalsIgnoreCase("freeze-message") && this.messageManager.getFreezeInterval() > 0) {
                this.messageManager.addFreezePlayer(onlineFreezee, totalMsg);
            } else if (playerPath.equalsIgnoreCase("freeze-location-message") && this.messageManager.getFreezeLocInterval() > 0) {
                this.messageManager.addFreezeLocPlayer(onlineFreezee, totalMsg);
            }

        }

        Player freezerP = frozenPlayer.getFreezerUUID() == null ? null : Bukkit.getPlayer(frozenPlayer.getFreezerUUID()) == null ? null : Bukkit.getPlayer(frozenPlayer.getFreezerUUID());
        for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
            if (freezerP != null) {
                freezerP.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reason)));
            } else {
                Bukkit.getConsoleSender().sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reason)));
            }

        }
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.hasPermission("sf.notify.freeze") && p != freezerP) {
                for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
                    p.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}",
                            timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reason)));

                }
            }
        }
    }

    public void notifyOfSQLFreeze(UUID freezeeUUID, String serversPlaceholder, String server) {
        UUID freezerUUID;
        try {
            freezerUUID = UUID.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + freezeeUUID.toString() + ".freezer-uuid"));
        } catch (IllegalArgumentException e) {
            freezerUUID = null;
        }
        Player onlineFreezee = Bukkit.getPlayer(freezeeUUID);
        String freezeeName = Bukkit.getPlayer(freezeeUUID) == null ? Bukkit.getOfflinePlayer(freezeeUUID).getName() : Bukkit.getPlayer(freezeeUUID).getName();
        String freezerName = freezerUUID == null ? "CONSOLE" : Bukkit.getPlayer(freezerUUID) == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer(freezerUUID).getName();
        String timePlaceholder = "Permanent";
        String locationPlaceholder = this.plugin.getConfig().getString("location");
        String reason = this.plugin.getPlayerConfig().getConfig().getString("players." + freezeeUUID.toString() + ".reason", "");

        String playerPath;
        String notifyPath;

        if (this.plugin.getPlayerConfig().getConfig().isSet("players." + freezeeUUID.toString() + ".unfreeze-date")) {
            timePlaceholder = TimeUtil.formatTime((this.plugin.getPlayerConfig().getConfig().getLong("players." + freezeeUUID.toString() + ".unfreeze-date") - System.currentTimeMillis()) / 1000L);
            playerPath = "sql-temp-freeze-message";
            notifyPath = "sql-temp-frozen-notify-message";
        } else {
            playerPath = "sql-freeze-message";
            notifyPath = "sql-frozen-notify-message";
        }

        if (onlineFreezee != null) {
            String totalMsg = "";
            for (String msg : this.plugin.getConfig().getStringList(playerPath)) {
                if (msg.equals("")) {
                    msg = " ";
                }
                totalMsg += msg + "\n";
                onlineFreezee.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reason).replace("{SERVER}", server)));
            }
            totalMsg = totalMsg.length() > 2 ? totalMsg.substring(0, totalMsg.length() - 1) : "";
            totalMsg = totalMsg.replace("{PLAYER}", freezeeName).replace("{FREEZER}", freezerName).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reason).replace("{SERVER}", server);
            if (playerPath.equalsIgnoreCase("sql-temp-freeze-message")) {
                this.messageManager.addTempFreezePlayer(onlineFreezee, totalMsg);
            } else if (playerPath.equalsIgnoreCase("sql-freeze-message")) {
                this.messageManager.addFreezePlayer(onlineFreezee, totalMsg);
            }

        }

        Player freezerP = freezerUUID == null ? null : Bukkit.getPlayer(freezerUUID) == null ? null : Bukkit.getPlayer(freezerUUID);
        for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
            if (freezerP != null) {
                freezerP.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reason).replace("{SERVER}", server)));
            } else {
                Bukkit.getConsoleSender().sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reason).replace("{SERVER}", server)));
            }

        }

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.hasPermission("sf.notify.freeze") && p != freezerP) {
                for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
                    p.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}",
                            timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reason).replace("{SERVER}", server)));

                }
            }
        }
    }

    public void notifyOfUnfreeze(CommandSender sender, UUID uuid, String freezeeName, String server) {
        Player onlineFreezee = Bukkit.getPlayer(uuid);
        String unfreezerName = sender.getName();

        String playerPath = server == null ? "unfreeze-message" : server.equals(this.plugin.getServerID()) ? "unfreeze-message" : "sql-unfreeze-message";
        String notifyPath = server == null ? "unfrozen-notify-message" : server.equals(this.plugin.getServerID()) ? "unfrozen-notify-message" : "sql-unfrozen-notify-message";

        if (onlineFreezee != null) {
            for (String msg : this.plugin.getConfig().getStringList(playerPath)) {
                onlineFreezee.sendMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerName).replace("{PLAYER}", freezeeName)));
            }
        }

        for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
            sender.sendMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerName).replace("{PLAYER}", freezeeName)));
        }
        Player senderP = sender instanceof Player ? (Player) sender : null;
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.hasPermission("sf.notify.unfreeze") && p != senderP) {
                for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
                    p.sendMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerName).replace("{PLAYER}", freezeeName)));
                }
            }
        }
    }

    public void notifyOfUnfreeze(UUID uuid, String freezeeName, String unfreezerName, String server) {
        Player onlineFreezee = Bukkit.getPlayer(uuid);
        String playerPath = server == null ? "unfreeze-message" : server.equals(this.plugin.getServerID()) ? "unfreeze-message" : "sql-unfreeze-message";
        String notifyPath = server == null ? "unfrozen-notify-message" : server.equals(this.plugin.getServerID()) ? "unfrozen-notify-message" : "sql-unfrozen-notify-message";

        if (onlineFreezee != null) {
            for (String msg : this.plugin.getConfig().getStringList(playerPath)) {
                onlineFreezee.sendMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerName).replace("{PLAYER}", freezeeName).replace("{SERVER}", server)));
            }
        }

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.hasPermission("sf.notify.unfreeze")) {
                for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
                    p.sendMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerName).replace("{PLAYER}", freezeeName).replace("{SERVER}", server)));
                }
            }
        }

        if (unfreezerName.equals("CONSOLE")) {
            for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
                Bukkit.getConsoleSender().sendMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerName).replace("{PLAYER}", freezeeName).replace("{SERVER}", server)));
            }
        }
    }

    public void notifyOfFreezeAll(UUID freezerUUID, String location) {
        String freezerName = freezerUUID == null ? "CONSOLE" : Bukkit.getPlayer(freezerUUID) == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer(freezerUUID).getName();
        String locPlaceholder = this.locationManager.getLocationPlaceholder(location);
        String reason = this.plugin.getPlayerConfig().getConfig().getString("freezeall-info.reason", this.plugin.getConfig().getString("default-reason"));

        String totalMsg = "";
        if (location != null) {
            for (String msg : this.plugin.getConfig().getStringList("freezeall-location-message")) {
                Bukkit.broadcastMessage(this.plugin.placeholders(msg.replace("{LOCATION}", locPlaceholder).replace("{FREEZER}", freezerName).replace("{REASON}", reason)));
                totalMsg += msg + "\n";
            }
        } else {
            for (String msg : this.plugin.getConfig().getStringList("freezeall-message")) {
                if (msg.equals("")) {
                    msg = " ";
                }
                Bukkit.broadcastMessage(this.plugin.placeholders(msg.replace("{LOCATION}", locPlaceholder).replace("{FREEZER}", freezerName).replace("{REASON}", reason)));
                totalMsg += msg + "\n";
            }
        }
        if (totalMsg.length() > 0) {
            totalMsg = totalMsg.substring(0, totalMsg.length() - 2);

        }
        totalMsg = this.plugin.placeholders(totalMsg.replace("{LOCATION}", locPlaceholder).replace("{FREEZER}", freezerName).replace("{REASON}", reason));

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (this.messageManager.getFreezeAllLocInterval() > 0) {
                this.messageManager.addFreezeAllPlayer(p, totalMsg);
            } else if (this.messageManager.getFreezeAllLocInterval() > 0) {
                this.messageManager.addFreezeAllLocPlayer(p, totalMsg);
            }
        }
    }

    public void notifyOfUnfreezeAll(UUID unfreezerUUID) {
        String unfreezerName = unfreezerUUID == null ? "CONSOLE" : Bukkit.getPlayer(unfreezerUUID) == null ? Bukkit.getOfflinePlayer(unfreezerUUID).getName() : Bukkit.getPlayer(unfreezerUUID).getName();
        for (String msg : this.plugin.getConfig().getStringList("unfreezeall-message")) {
            Bukkit.broadcastMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerName)));
        }
    }

}
