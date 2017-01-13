package org.plugins.simplefreeze.managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.cache.FrozenPages;
import org.plugins.simplefreeze.objects.FrozenPlayer;
import org.plugins.simplefreeze.objects.SFLocation;
import org.plugins.simplefreeze.objects.TempFrozenPlayer;
import org.plugins.simplefreeze.util.TimeUtil;

import java.util.UUID;

public class FreezeManager {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;
    private final HelmetManager helmetManager;
    private final LocationManager locationManager;
    private final SQLManager sqlManager;
    private final FrozenPages frozenPages;

    public FreezeManager(SimpleFreezeMain plugin, PlayerManager playerManager, HelmetManager helmetManager, LocationManager locationManager, SQLManager sqlManager, FrozenPages frozenPages) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.helmetManager = helmetManager;
        this.locationManager = locationManager;
        this.sqlManager = sqlManager;
        this.frozenPages = frozenPages;
    }

    public void unfreeze(UUID uuid) {
        if (this.playerManager.isFrozen(uuid)) {
            FrozenPlayer frozenPlayer = this.playerManager.getFrozenPlayer(uuid);
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.getInventory().setHelmet(frozenPlayer.getHelmet());
                p.teleport(frozenPlayer.getOriginalLoc());
                if (this.plugin.getConfig().getBoolean("enable-fly")) {
                    p.setAllowFlight(false);
                    p.setFlying(false);
                }
            }
            this.plugin.getPlayerConfig().getConfig().set("players." + uuid.toString(), null);
            this.plugin.getPlayerConfig().saveConfig();
            this.plugin.getPlayerConfig().reloadConfig();
            this.playerManager.removeFrozenPlayer(uuid);
        }
    }

    public void freeze(UUID freezeeUUID, UUID freezerUUID, String location) {
        ItemStack helmet = null;
        SFLocation originalLoc = null;
        SFLocation freezeLoc = null;
        Player onlineFreezee = Bukkit.getPlayer(freezeeUUID);
        String freezeeName = Bukkit.getPlayer(freezeeUUID) == null ? Bukkit.getOfflinePlayer(freezeeUUID).getName() : Bukkit.getPlayer(freezeeUUID).getName();
        String freezerName = freezerUUID == null ? "CONSOLE" : Bukkit.getPlayer(freezerUUID) == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer
                (freezerUUID).getName();

        if (location != null) {
            if (this.plugin.getConfig().isSet("locations." + location) && this.plugin.getConfig().isSet("locations." + location + ".worldname") && this.plugin.getConfig().isSet("locations." + location + ".x")
                    && this.plugin.getConfig().isSet("locations." + location + ".y") && this.plugin.getConfig().isSet("locations." + location + ".z")) {
                World world = Bukkit.getWorld(this.plugin.getConfig().getString("locations." + location + ".worldname"));
                double x = this.plugin.getConfig().getDouble("locations." + location + ".x");
                double y = this.plugin.getConfig().getDouble("locations." + location + ".y");
                double z = this.plugin.getConfig().getDouble("locations." + location + ".z");
                float yaw = this.plugin.getConfig().isSet("locations." + location + ".yaw") ? Float.valueOf(this.plugin.getConfig().getString("locations." + location + ".yaw")) : (float) 0.0;
                if (!this.plugin.getConfig().isSet("locations." + location + ".yaw") && onlineFreezee != null) {
                    yaw = onlineFreezee.getLocation().getYaw();
                }
                float pitch = this.plugin.getConfig().isSet("locations." + location + ".pitch") ? Float.valueOf(this.plugin.getConfig().getString("locations." + location + ".pitch")) : (float) 0.0;
                if (!this.plugin.getConfig().isSet("locations." + location + ".pitch") && onlineFreezee != null) {
                    pitch = onlineFreezee.getLocation().getPitch();
                }
                freezeLoc = new SFLocation(world, x, y, z, yaw, pitch);
            }
        }

        if (onlineFreezee != null) {
            if (onlineFreezee.getInventory().getHelmet() != null) {
                helmet = onlineFreezee.getInventory().getHelmet();
            }
            onlineFreezee.getInventory().setHelmet(this.helmetManager.getPersonalHelmetItem(freezeeName, freezerName, location, null));
            originalLoc = new SFLocation(onlineFreezee.getLocation());
            if (freezeLoc == null && this.plugin.getConfig().getBoolean("teleport-up")) {
                freezeLoc = this.locationManager.getHighestAirLocation(originalLoc);
            }
            else if (freezeLoc == null) {
                freezeLoc = new SFLocation(originalLoc.clone());
                if (this.plugin.getConfig().getBoolean("enable-fly")) {
                    onlineFreezee.setAllowFlight(true);
                    onlineFreezee.setFlying(true);
                }
            }
            if (!freezeLoc.equals(originalLoc)) {
                onlineFreezee.teleport(freezeLoc);
            }
        }

        long freezeDate = System.currentTimeMillis();
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freeze-date", freezeDate);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freezer-uuid", freezerName.equals("CONSOLE") ? "null" : Bukkit.getPlayerExact(freezerName).getUniqueId().toString());
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".original-location", originalLoc == null ? "null" : originalLoc.toString());
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freeze-location", freezeLoc == null ? "null" : freezeLoc.toString());
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".mysql", false);
        if (onlineFreezee == null) {
            this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".message", true);
        }
        this.plugin.getPlayerConfig().saveConfig();
        this.plugin.getPlayerConfig().reloadConfig();

        if (onlineFreezee != null) {
            FrozenPlayer frozenPlayer = new FrozenPlayer(freezeDate, freezeeUUID, freezerUUID, originalLoc, freezeLoc, false, helmet);
            this.playerManager.addFrozenPlayer(freezeeUUID, frozenPlayer);
        }
        else {
            this.frozenPages.refreshString(freezeeUUID);
        }
    }

    public void tempFreeze(final UUID freezeeUUID, UUID freezerUUID, String location, long time) {
        ItemStack helmet = null;
        SFLocation originalLoc = null;
        SFLocation freezeLoc = null;
        final Player onlineFreezee = Bukkit.getPlayer(freezeeUUID);
        final String freezeeName = Bukkit.getPlayer(freezeeUUID) == null ? Bukkit.getOfflinePlayer(freezeeUUID).getName() : Bukkit.getPlayer(freezeeUUID).getName();
        String freezerName = freezerUUID == null ? "CONSOLE" : Bukkit.getPlayer(freezerUUID) == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer
                (freezerUUID).getName();

        if (location != null) {
            if (this.plugin.getConfig().isSet("locations." + location) && this.plugin.getConfig().isSet("locations." + location + ".worldname") && this.plugin.getConfig().isSet("locations." + location + ".x")
                    && this.plugin.getConfig().isSet("locations." + location + ".y") && this.plugin.getConfig().isSet("locations." + location + ".z")) {
                World world = Bukkit.getWorld(this.plugin.getConfig().getString("locations." + location + ".worldname"));
                double x = this.plugin.getConfig().getDouble("locations." + location + ".x");
                double y = this.plugin.getConfig().getDouble("locations." + location + ".y");
                double z = this.plugin.getConfig().getDouble("locations." + location + ".z");
                float yaw = this.plugin.getConfig().isSet("locations." + location + ".yaw") ? Float.valueOf(this.plugin.getConfig().getString("locations." + location + ".yaw")) : (float) 0.0;
                if (!this.plugin.getConfig().isSet("locations." + location + ".yaw") && onlineFreezee != null) {
                    yaw = onlineFreezee.getLocation().getYaw();
                }
                float pitch = this.plugin.getConfig().isSet("locations." + location + ".pitch") ? Float.valueOf(this.plugin.getConfig().getString("locations." + location + ".pitch")) : (float) 0.0;
                if (!this.plugin.getConfig().isSet("locations." + location + ".pitch") && onlineFreezee != null) {
                    pitch = onlineFreezee.getLocation().getPitch();
                }
                freezeLoc = new SFLocation(world, x, y, z, yaw, pitch);
            }
        }

        if (onlineFreezee != null) {
            if (onlineFreezee.getInventory().getHelmet() != null) {
                helmet = onlineFreezee.getInventory().getHelmet();
            }
            onlineFreezee.getInventory().setHelmet(this.helmetManager.getPersonalHelmetItem(freezeeName, freezerName, location, time));
            originalLoc = new SFLocation(onlineFreezee.getLocation());
            if (freezeLoc == null && this.plugin.getConfig().getBoolean("teleport-up")) {
                freezeLoc = this.locationManager.getHighestAirLocation(originalLoc);
            }
            else if (freezeLoc == null) {
                freezeLoc = new SFLocation(originalLoc.clone());
                if (this.plugin.getConfig().getBoolean("enable-fly")) {
                    onlineFreezee.setAllowFlight(true);
                    onlineFreezee.setFlying(true);
                }
            }
            if (!freezeLoc.equals(originalLoc)) {
                onlineFreezee.teleport(freezeLoc);
            }
        }

        long freezeDate = System.currentTimeMillis();
        long unfreezeDate = freezeDate + (time * 1000L);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freeze-date", freezeDate);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".unfreeze-date", unfreezeDate);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freezer-uuid", freezerName.equals("CONSOLE") ? "null" : Bukkit.getPlayerExact(freezerName).getUniqueId().toString());
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".original-location", originalLoc == null ? "null" : originalLoc.toString());
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freeze-location", freezeLoc == null ? "null" : freezeLoc.toString());
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".mysql", false);
        if (onlineFreezee == null) {
            this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".message", true);
        }
        this.plugin.getPlayerConfig().saveConfig();
        this.plugin.getPlayerConfig().reloadConfig();
        final TempFrozenPlayer tempFrozenPlayer = new TempFrozenPlayer(freezeDate, unfreezeDate, freezeeUUID, freezerUUID, originalLoc, freezeLoc, false, helmet);
        if (onlineFreezee != null) {
            tempFrozenPlayer.startTask(this.plugin);
            this.playerManager.addFrozenPlayer(freezeeUUID, tempFrozenPlayer);
        }
        else {
            this.frozenPages.refreshString(freezeeUUID);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                frozenPages.removePlayer(tempFrozenPlayer.getFreezeeUUID());
            }
        }.runTaskLater(plugin, (tempFrozenPlayer.getUnfreezeDate() - System.currentTimeMillis()) / 1000L * 20L);
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
        String timePlaceholder = "";
        String serversPlaceholder = "";
        String locationPlaceholder = this.locationManager.getLocationPlaceholder(location);
        String playerPath;
        String notifyPath;
        if (this.plugin.getPlayerConfig().getConfig().isSet("players." + freezeeUUID.toString() + ".unfreeze-date")) {
            timePlaceholder = TimeUtil.formatTime((this.plugin.getPlayerConfig().getConfig().getLong("players." + freezeeUUID.toString() + ".unfreeze-date") - System.currentTimeMillis()) / 1000L);
            playerPath = "temp-freeze-message";
            if (location != null) {
                playerPath = "temp-freeze-location-message";
            }
            notifyPath = "temp-frozen-notify-message";
            if (this.plugin.getPlayerConfig().getConfig().getBoolean("players." + freezeeUUID.toString() + ".mysql")) {
                notifyPath = "sql-temp-frozen-notify-message";
            }
        }
        else {
            playerPath = "freeze-message";
            if (location != null) {
                playerPath = "freeze-location-message";
            }
            notifyPath = "frozen-notify-message";
            if (this.plugin.getPlayerConfig().getConfig().getBoolean("players." + freezeeUUID.toString() + ".mysql")) {
                notifyPath = "sql-frozen-notify-message";
            }
        }

        if (onlineFreezee != null) {
            for (String msg : this.plugin.getConfig().getStringList(playerPath)) {
                if (!msg.equals("")) {
                    onlineFreezee.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
                }
            }
        }
        Player freezerP = freezerUUID == null ? null : Bukkit.getPlayer(freezerUUID) == null ? null : Bukkit.getPlayer(freezerUUID);
        for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
            if (!msg.equals("")) {
                if (freezerP != null) {
                    freezerP.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
                }
                else {
                    Bukkit.getConsoleSender().sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
                }
            }
        }
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.hasPermission("sf.notify.freeze") && p != freezerP) {
                for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
                    if (!msg.equals("")) {
                        if (freezerP != null) {
                            freezerP.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}",
                                    timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
                        }
                        else {
                            Bukkit.getConsoleSender().sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}",
                                    timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
                        }
                    }
                }
            }
        }
    }

    public void notifyOfFreeze(FrozenPlayer frozenPlayer) {
        String location = this.locationManager.getLocationName(frozenPlayer.getFreezeLoc());
        Player onlineFreezee = Bukkit.getPlayer(frozenPlayer.getFreezeeUUID());
        String freezeeName = frozenPlayer.getFreezeeName();
        String freezerName = frozenPlayer.getFreezerName();
        String timePlaceholder = "";
        String serversPlaceholder = "";
        String locationPlaceholder = this.locationManager.getLocationPlaceholder(location);
        String playerPath;
        String notifyPath;
        if (frozenPlayer instanceof TempFrozenPlayer) {
            timePlaceholder = TimeUtil.formatTime((((TempFrozenPlayer) frozenPlayer).getUnfreezeDate() - System.currentTimeMillis()) / 1000L);
            playerPath = "temp-freeze-message";
            if (location != null) {
                playerPath = "temp-freeze-location-message";
            }
            notifyPath = "temp-frozen-notify-message";
            if (frozenPlayer.isSqlFreeze()) {
                notifyPath = "sql-temp-frozen-notify-message";
            }
        }
        else {
            playerPath = "freeze-message";
            if (location != null) {
                playerPath = "freeze-location-message";
            }
            notifyPath = "frozen-notify-message";
            if (frozenPlayer.isSqlFreeze()) {
                notifyPath = "sql-frozen-notify-message";
            }
        }

        if (onlineFreezee != null) {
            for (String msg : this.plugin.getConfig().getStringList(playerPath)) {
                if (!msg.equals("")) {
                    onlineFreezee.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
                }
            }
        }
        Player freezerP = frozenPlayer.getFreezerUUID() == null ? null : Bukkit.getPlayer(frozenPlayer.getFreezerUUID()) == null ? null : Bukkit.getPlayer(frozenPlayer.getFreezerUUID());
        for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
            if (!msg.equals("")) {
                if (freezerP != null) {
                    freezerP.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
                }
                else {
                    Bukkit.getConsoleSender().sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
                }
            }
        }
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.hasPermission("sf.notify.freeze") && p != freezerP) {
                for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
                    if (!msg.equals("")) {
                        if (freezerP != null) {
                            freezerP.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}",
                                    timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
                        }
                        else {
                            Bukkit.getConsoleSender().sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerName).replace("{PLAYER}", freezeeName).replace("{TIME}",
                                    timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
                        }
                    }
                }
            }
        }
    }

    public void notifyOfUnfreeze(CommandSender sender, UUID uuid, String freezeeName) {
        Player onlineFreezee = Bukkit.getPlayer(uuid);
        String unfreezerName = sender.getName();

        if (onlineFreezee != null) {
            for (String msg : this.plugin.getConfig().getStringList("unfreeze-message")) {
                if (!msg.equals("")) {
                    onlineFreezee.sendMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerName).replace("{PLAYER}", freezeeName)));
                }
            }
        }

        for (String msg : this.plugin.getConfig().getStringList("unfrozen-notify-message")) {
            if (!msg.equals("")) {
                sender.sendMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerName).replace("{PLAYER}", freezeeName)));
            }
        }
        Player senderP = sender instanceof Player ? (Player) sender : null;
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.hasPermission("sf.notify.unfreeze") && p != senderP) {
                for (String msg : this.plugin.getConfig().getStringList("unfrozen-notify-message")) {
                    if (!msg.equals("")) {
                        sender.sendMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerName).replace("{PLAYER}", freezeeName)));
                    }
                }
            }
        }
    }

}
