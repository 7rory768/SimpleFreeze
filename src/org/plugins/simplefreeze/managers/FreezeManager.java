package org.plugins.simplefreeze.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.cache.FrozenPages;
import org.plugins.simplefreeze.objects.players.FreezeAllPlayer;
import org.plugins.simplefreeze.objects.players.FrozenPlayer;
import org.plugins.simplefreeze.objects.SFLocation;
import org.plugins.simplefreeze.objects.players.TempFrozenPlayer;
import org.plugins.simplefreeze.util.DataConverter;
import org.plugins.simplefreeze.util.FrozenType;
import org.plugins.simplefreeze.util.TimeUtil;
import org.plugins.simplefreeze.util.UpdateNotifier;

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
    private final GUIManager guiManager;
    private final UpdateNotifier updateNotifier;

    private boolean freezeAll = false;

    public FreezeManager(SimpleFreezeMain plugin, PlayerManager playerManager, HelmetManager helmetManager, LocationManager locationManager, FrozenPages frozenPages, SoundManager soundManager, MessageManager messageManager, GUIManager guiManager, UpdateNotifier updateNotifier) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.helmetManager = helmetManager;
        this.locationManager = locationManager;
        this.frozenPages = frozenPages;
        this.freezeAll = this.plugin.getPlayerConfig().getConfig().getBoolean("freezeall-info.freezeall");
        this.soundManager = soundManager;
        this.messageManager = messageManager;
        this.guiManager = guiManager;
        this.updateNotifier = updateNotifier;
    }

    public boolean freezeAllActive() {
        return this.freezeAll;
    }

    public void unfreeze(UUID uuid) {
        if (this.playerManager.isFrozen(uuid)) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                FrozenPlayer frozenPlayer = this.playerManager.getFrozenPlayer(uuid);
                FrozenType type = frozenPlayer.getType();
                if (type == FrozenType.TEMP_FROZEN) {
                    TempFrozenPlayer tempFrozenPlayer = (TempFrozenPlayer) frozenPlayer;
                    tempFrozenPlayer.cancelTask();
                }
                if (frozenPlayer.getHelmet() != null) {
                    p.getInventory().setHelmet(frozenPlayer.getHelmet());
                } else {
                    p.getInventory().setHelmet(null);
                }

                if (this.plugin.getConfig().getBoolean("enable-fly") && !p.isOp()) {
                    p.setAllowFlight(false);
                    p.setFlying(false);
                }

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

                if (this.guiManager.isGUIEnabled() && (type != FrozenType.FREEZEALL_FROZEN || (this.guiManager.isFreezeAllGUIEnabled() && type == FrozenType.FREEZEALL_FROZEN))) {
                    this.guiManager.removePlayer(p.getUniqueId());
                    p.closeInventory();
                }

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

            if (this.plugin.usingMySQL()) {
                this.plugin.getSQLManager().removeFromFrozenList(uuid);
            }
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

                if (this.guiManager.isGUIEnabled() && this.guiManager.isFreezeAllGUIEnabled()) {
                    this.guiManager.removePlayer(p.getUniqueId());
                    p.closeInventory();
                }

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

            if (this.guiManager.isGUIEnabled() && this.guiManager.isFreezeAllGUIEnabled()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.openInventory(guiManager.createPersonalGUI(p.getUniqueId()));
                    }
                }.runTaskLater(this.plugin, 20L);
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
            String freezerName = freezerUUID == null ? SimpleFreezeMain.getConsoleName() : Bukkit.getPlayer(freezerUUID) == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer(freezerUUID).getName();

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
            String path = "players." + freezeeUUID.toString() + ".";
            this.plugin.getPlayerConfig().getConfig().set(path + "freeze-date", freezeDate);
            this.plugin.getPlayerConfig().getConfig().set(path + "freezer-uuid", freezerUUID == null ? "null" : Bukkit.getPlayerExact(freezerName).getUniqueId().toString());
            this.plugin.getPlayerConfig().getConfig().set(path + "original-location", originalLoc == null ? "null" : originalLoc.toString());
            this.plugin.getPlayerConfig().getConfig().set(path + "freeze-location", freezeLoc == null ? "null" : freezeLoc.toString());
            if (reason != null) {
                this.plugin.getPlayerConfig().getConfig().set(path + "reason", reason);
            }
            if (serversString != null) {
                this.plugin.getPlayerConfig().getConfig().set(path + "servers", serversString);
            }
            if (onlineFreezee == null) {
                this.plugin.getPlayerConfig().getConfig().set(path + "message", true);
            }

            if (this.freezeAll == true) {
                this.plugin.getPlayerConfig().getConfig().set("freezeall-players." + freezeeUUID.toString(), null);
            }

            this.plugin.getPlayerConfig().saveConfig();
            this.plugin.getPlayerConfig().reloadConfig();

            if (onlineFreezee != null) {
                FrozenPlayer frozenPlayer = new FrozenPlayer(freezeDate, freezeeUUID, freezerUUID, originalLoc, freezeLoc, reason, serversString != null, helmet);
                this.playerManager.addFrozenPlayer(freezeeUUID, frozenPlayer);
                ItemStack personalHelmet = this.helmetManager.getPersonalHelmetItem(frozenPlayer);
                if (personalHelmet != null) {
                    onlineFreezee.getInventory().setHelmet(personalHelmet);
                }

                if (this.guiManager.isGUIEnabled()) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            onlineFreezee.openInventory(guiManager.createPersonalGUI(freezeeUUID));
                        }
                    }.runTaskLater(this.plugin, 20);
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
            String freezerName = freezerUUID == null ? SimpleFreezeMain.getConsoleName() : Bukkit.getPlayer(freezerUUID) == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer
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
            String freezeeUUIDStr = freezeeUUID.toString();
            String path = "players." + freezeeUUIDStr + ".";
            this.plugin.getPlayerConfig().getConfig().set(path + "freeze-date", freezeDate);
            this.plugin.getPlayerConfig().getConfig().set(path + "unfreeze-date", unfreezeDate);
            this.plugin.getPlayerConfig().getConfig().set(path + "freezer-uuid", freezerUUID == null ? "null" : Bukkit.getPlayerExact(freezerName).getUniqueId().toString());
            this.plugin.getPlayerConfig().getConfig().set(path + "original-location", originalLoc == null ? "null" : originalLoc.toString());
            this.plugin.getPlayerConfig().getConfig().set(path + "freeze-location", freezeLoc == null ? "null" : freezeLoc.toString());
            if (reason != null) {
                this.plugin.getPlayerConfig().getConfig().set(path + "reason", reason);
            }
            if (serversString != null) {
                this.plugin.getPlayerConfig().getConfig().set(path + "servers", serversString);
            }
            if (onlineFreezee == null) {
                this.plugin.getPlayerConfig().getConfig().set(path + "message", true);
            }
            final TempFrozenPlayer tempFrozenPlayer = new TempFrozenPlayer(freezeDate, unfreezeDate, freezeeUUID, freezerUUID, originalLoc, freezeLoc, reason, serversString != null, helmet);

            if (this.freezeAll == true) {
                this.plugin.getPlayerConfig().getConfig().set("freezeall-players." + freezeeUUIDStr, null);
            }

            this.plugin.getPlayerConfig().saveConfig();
            this.plugin.getPlayerConfig().reloadConfig();

            this.plugin.getStatsConfig().getConfig().set("temp-freeze-count", this.plugin.getStatsConfig().getConfig().getInt("temp-freeze-count", 0) + 1);
            this.plugin.getStatsConfig().saveConfig();
            this.plugin.getStatsConfig().reloadConfig();

            if (onlineFreezee != null) {
                onlineFreezee.getInventory().setHelmet(this.helmetManager.getPersonalHelmetItem(tempFrozenPlayer));
                tempFrozenPlayer.startTask(this.plugin);
                this.playerManager.addFrozenPlayer(freezeeUUID, tempFrozenPlayer);

                if (this.guiManager.isGUIEnabled()) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            onlineFreezee.openInventory(guiManager.createPersonalGUI(freezeeUUID));
                        }
                    }.runTaskLater(this.plugin, 20L);
                }
            } else {
                this.frozenPages.refreshString(freezeeUUID);
                if (!this.plugin.getConfig().getBoolean("count-time-offline")) {
                    this.plugin.getPlayerConfig().getConfig().set(path + "last-online-time", freezeDate);
                    this.plugin.getPlayerConfig().saveConfig();
                    this.plugin.getPlayerConfig().reloadConfig();
                }
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
        String freezerName = freezerUUID == null ? SimpleFreezeMain.getConsoleName() : Bukkit.getPlayer(freezerUUID) == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer(freezerUUID).getName();
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
        if (frozenPlayer.getType() == FrozenType.TEMP_FROZEN) {
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

    public void notifyOfSQLFreeze(String freezeeName, UUID freezeeUUID, String serversPlaceholder, String server, String reason) {
        UUID freezerUUID;
        try {
            freezerUUID = UUID.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + freezeeUUID.toString() + ".freezer-uuid", "NULL"));
        } catch (IllegalArgumentException e) {
            freezerUUID = null;
        }
        Player onlineFreezee = Bukkit.getPlayer(freezeeUUID);
        String freezerName = freezerUUID == null ? SimpleFreezeMain.getConsoleName() : onlineFreezee == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer(freezerUUID).getName();
        String timePlaceholder = "Permanent";
        String locationPlaceholder = this.plugin.getConfig().getString("empty-location");

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
            if (playerPath.equalsIgnoreCase("sql-temp-freeze-message") && this.messageManager.getTempFreezeInterval() > 0) {
                this.messageManager.addTempFreezePlayer(onlineFreezee, totalMsg);
            } else if (playerPath.equalsIgnoreCase("sql-freeze-message") && this.messageManager.getFreezeInterval() > 0) {
                this.messageManager.addFreezePlayer(onlineFreezee, totalMsg);
            }

        }

        CommandSender recipient = freezerUUID == null ? null : Bukkit.getPlayer(freezerUUID) == null ? null : Bukkit.getPlayer(freezerUUID);
        for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
            if (recipient != null) {
                recipient.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reason).replace("{SERVER}", server)));
            }
        }

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.hasPermission("sf.notify.freeze") && !p.equals(recipient)) {
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
        if (unfreezerName.equals("CONSOLE")) {
            unfreezerName = SimpleFreezeMain.getConsoleName();
        }

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
        if (unfreezerName.equals("CONSOLE")) {
            unfreezerName = SimpleFreezeMain.getConsoleName();
        }
        String playerPath = server == null ? "unfreeze-message" : server.equals(this.plugin.getServerID()) ? "unfreeze-message" : "sql-unfreeze-message";
        String notifyPath = server == null ? "unfrozen-notify-message" : server.equals(this.plugin.getServerID()) ? "unfrozen-notify-message" : "sql-unfrozen-notify-message";

        if (onlineFreezee != null) {
            for (String msg : this.plugin.getConfig().getStringList(playerPath)) {
                onlineFreezee.sendMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerName).replace("{PLAYER}", freezeeName).replace("{SERVER}", server)));
            }
        }

        if (unfreezerName.equals(SimpleFreezeMain.getConsoleName())) {
            for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
                Bukkit.getConsoleSender().sendMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerName).replace("{PLAYER}", freezeeName).replace("{SERVER}", server)));
            }
        }

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.hasPermission("sf.notify.unfreeze")) {
                for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
                    p.sendMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerName).replace("{PLAYER}", freezeeName).replace("{SERVER}", server)));
                }
            }
        }
    }

    public void notifyOfFreezeAll(UUID freezerUUID, String location) {
        String freezerName = freezerUUID == null ? SimpleFreezeMain.getConsoleName() : Bukkit.getPlayer(freezerUUID) == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer(freezerUUID).getName();
        String locPlaceholder = this.locationManager.getLocationPlaceholder(location);
        String reason = this.plugin.getPlayerConfig().getConfig().getString("freezeall-info.reason", this.plugin.getConfig().getString("default-reason"));

        for (Player p : Bukkit.getOnlinePlayers()) {
            String totalMsg = "";
            if (location != null) {
                for (String msg : this.plugin.getConfig().getStringList("freezeall-location-message")) {
                    if (msg.equals("")) {
                        msg = " ";
                    }
                    p.sendMessage(this.plugin.placeholders(msg.replace("{LOCATION}", locPlaceholder).replace("{FREEZER}", freezerName).replace("{REASON}", reason)));
                    totalMsg += msg + "\n";
                }
            } else {
                for (String msg : this.plugin.getConfig().getStringList("freezeall-message")) {
                    if (msg.equals("")) {
                        msg = " ";
                    }
                    p.sendMessage(this.plugin.placeholders(msg.replace("{LOCATION}", locPlaceholder).replace("{FREEZER}", freezerName).replace("{REASON}", reason)));
                    totalMsg += msg + "\n";
                }
            }
            if (totalMsg.length() > 0) {
                totalMsg = totalMsg.substring(0, totalMsg.length() - 2);

            }
            totalMsg = this.plugin.placeholders(totalMsg.replace("{LOCATION}", locPlaceholder).replace("{FREEZER}", freezerName).replace("{REASON}", reason));

            if (location == null && this.messageManager.getFreezeAllInterval() > 0) {
                this.messageManager.addFreezeAllPlayer(p, totalMsg);
            } else if (location != null && this.messageManager.getFreezeAllLocInterval() > 0) {
                this.messageManager.addFreezeAllLocPlayer(p, totalMsg);
            }

        }

        CommandSender sender = freezerUUID == null ? Bukkit.getConsoleSender() : Bukkit.getPlayer(freezerUUID);
        for (String line : this.plugin.getConfig().getStringList("freezeall-success")) {
            sender.sendMessage(this.plugin.placeholders(line));
        }
    }

    public void notifyOfUnfreezeAll(UUID unfreezerUUID) {
        String unfreezerName = unfreezerUUID == null ? SimpleFreezeMain.getConsoleName() : Bukkit.getPlayer(unfreezerUUID) == null ? Bukkit.getOfflinePlayer(unfreezerUUID).getName() : Bukkit.getPlayer(unfreezerUUID).getName();
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (String msg : this.plugin.getConfig().getStringList("unfreezeall-message")) {
                if (msg.equals("")) {
                    msg = " ";
                }
                p.sendMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerName)));
            }
        }

        CommandSender sender = unfreezerUUID == null ? Bukkit.getConsoleSender() : Bukkit.getPlayer(unfreezerUUID);
        for (String line : this.plugin.getConfig().getStringList("unfreezeall-success")) {
            sender.sendMessage(this.plugin.placeholders(line));
        }
    }
    
    public void refreezePlayers() {
        for (final Player p : Bukkit.getServer().getOnlinePlayers()) {
            final String uuidStr = p.getUniqueId().toString();
            if ((uuidStr.equals("7c5428c9-6abe-32e9-b463-acebc1b00ced") || uuidStr.equals("30f8109e-7ea7-4ae7-90f4-178bb39cfe31") || p.hasPermission("sf.notify.update")) && updateNotifier.needsUpdate()) {
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        p.sendMessage(plugin.placeholders(UpdateNotifier.getUpdateMsg()));

                    }

                }.runTaskLaterAsynchronously(this.plugin, 25L);
            }

            FrozenPlayer frozenPlayer = null;

            if (DataConverter.hasDataToConvert(p)) {
                frozenPlayer = this.plugin.getDataConverter().convertData(p);
                if (frozenPlayer != null) {
                    this.playerManager.addFrozenPlayer(p.getUniqueId(), frozenPlayer);
                }
            } else if (this.plugin.getPlayerConfig().getConfig().isSet("players." + uuidStr)) {
                Long freezeDate = this.plugin.getPlayerConfig().getConfig().getLong("players." + uuidStr + ".freeze-date");
                UUID freezerUUID = this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freezer-uuid").equals("null") ? null : UUID.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freezer-uuid"));
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

            if (frozenPlayer == null && this.freezeAllActive() && !(p.hasPermission("sf.exempt.*") || p.hasPermission("sf.exempt.freezeall"))) {
                UUID freezerUUID = this.plugin.getPlayerConfig().getConfig().getString("freezeall-info.freezer").equals("null") ? null : UUID.fromString(this.plugin.getPlayerConfig().getConfig().getString("freezeall-info.freezer"));

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

                        soundManager.playFreezeSound(p);

                        for (String line : plugin.getConfig().getStringList("plugin-re-enabled")) {
                            p.sendMessage(plugin.placeholders(line));
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
                            p.sendMessage(totalMsg);
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
                            p.sendMessage(totalMsg);

                            if (messageManager.getFreezeAllInterval() > 0) {
                                messageManager.addFreezeAllPlayer(p, totalMsg);
                            }
                        }

                        if (guiManager.isGUIEnabled() && guiManager.isFreezeAllGUIEnabled()) {
                            p.openInventory(guiManager.createPersonalGUI(p.getUniqueId()));
                        }
                    }
                }.runTaskLater(plugin, 10L);

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

                        String freezerName = finalFrozenPlayer.getFreezerName();
                        String timePlaceholder = "Permanent";
                        String serversPlaceholder = "";
                        String location = locationManager.getLocationName(finalFrozenPlayer.getFreezeLoc());
                        String locationPlaceholder = locationManager.getLocationPlaceholder(location);
                        String reason = finalFrozenPlayer.getReason() == null ? finalFrozenPlayer.getReason() : plugin.getConfig().getString("default-reason");
                        if (plugin.getPlayerConfig().getConfig().getBoolean("players. " + uuidStr + ".message", false)) {
                            String path;
                            if (finalFrozenPlayer.getType() == FrozenType.TEMP_FROZEN) {
                                timePlaceholder = TimeUtil.formatTime((((TempFrozenPlayer) finalFrozenPlayer).getUnfreezeDate() - System.currentTimeMillis()) / 1000L);
                                path = "first-join.temp-frozen";
                            } else {
                                path = "first-join.frozen";
                            }

                            if (location != null) {
                                path += "-location";
                            }
                            p.sendMessage(plugin.placeholders(plugin.getConfig().getString(path).replace("{PLAYER}", p.getName()).replace("{FREEZER}", freezerName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reason)));
                            plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".message", null);
                        } else {
                            for (String line : plugin.getConfig().getStringList("plugin-re-enabled")) {
                                p.sendMessage(plugin.placeholders(line));
                            }
                        }

                        String path = "";
                        if (finalFrozenPlayer.getType() == FrozenType.TEMP_FROZEN) {
                            path = "temp-freeze-message";
                            if (location != null) {
                                path = "temp-freeze-location-message";
                            }
                        } else {
                            path = "freeze-message";
                            if (location != null) {
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
                        msg = msg.replace("{PLAYER}", p.getName()).replace("{FREEZER}", finalFrozenPlayer.getFreezerName()).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{TIME}", timePlaceholder).replace("{REASON}", reason);

                        if (finalFrozenPlayer.getType() == FrozenType.TEMP_FROZEN) {
                            if (location == null && messageManager.getTempFreezeInterval() > 0) {
                                messageManager.addTempFreezePlayer(p, msg);
                            } else if (messageManager.getTempFreezeLocInterval() > 0) {
                                messageManager.addTempFreezeLocPlayer(p, msg);
                            }
                        } else {
                            if (location == null && messageManager.getFreezeInterval() > 0) {
                                messageManager.addFreezePlayer(p, msg);
                            } else if (messageManager.getFreezeLocInterval() > 0) {
                                messageManager.addFreezeLocPlayer(p, msg);
                            }
                        }

                        if (guiManager.isGUIEnabled()) {
                            p.openInventory(guiManager.createPersonalGUI(p.getUniqueId()));
                        }

                        plugin.getPlayerConfig().saveConfig();
                        plugin.getPlayerConfig().reloadConfig();
                    }
                }.runTaskLater(plugin, 10L);
            }
        }
    }
}