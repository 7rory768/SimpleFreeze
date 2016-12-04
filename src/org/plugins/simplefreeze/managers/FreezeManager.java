package org.plugins.simplefreeze.managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.plugins.simplefreeze.SimpleFreezeMain;
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

    public FreezeManager(SimpleFreezeMain plugin, PlayerManager playerManager, HelmetManager helmetManager, LocationManager locationManager, SQLManager sqlManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.helmetManager = helmetManager;
        this.locationManager = locationManager;
        this.sqlManager = sqlManager;
    }

    public void unfreeze(UUID uuid) {
        if (this.playerManager.isFrozen(uuid)) {
            FrozenPlayer frozenPlayer = this.playerManager.getFrozenPlayer(uuid);
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.getInventory().setHelmet(frozenPlayer.getHelmet());
                p.teleport(frozenPlayer.getOriginalLoc());
            }
            this.plugin.getPlayerConfig().getConfig().set("players." + uuid.toString(), null);
            this.plugin.getPlayerConfig().saveConfig();
            this.plugin.getPlayerConfig().reloadConfig();
            this.playerManager.removeFrozenPlayer(uuid);
        }
    }

    public void freeze(UUID freezeeUUID, String freezeeName, String freezerName, String location) {
        ItemStack helmet = null;
        SFLocation originalLoc = null;
        SFLocation freezeLoc = null;
        Player onlineFreezee = Bukkit.getPlayer(freezeeUUID);
        String playerPlaceholder = freezeeName;
        String freezerPlaceholder = freezerName;
        String locationPlaceholder = location == null ? this.plugin.getConfig().getString("location") : this.plugin.getConfig().getString("locations." + location + ".placeholder", location);
        String timePlaceholder = "";
        String serversPlaceholder = "";

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
            } else if (freezeLoc == null) {
                freezeLoc = new SFLocation(originalLoc.clone());
                if (this.plugin.getConfig().getBoolean("enable-fly")) {
                    onlineFreezee.setFlying(true);
                }
            }
            if (!freezeLoc.equals(originalLoc)) {
                onlineFreezee.teleport(freezeLoc);
            }
        }

        long freezeDate = System.currentTimeMillis();
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freeze-date", freezeDate);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freezee-name", freezeeName);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freezer-name", freezerName);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".orginal-location", originalLoc == null ? "null" : originalLoc.toString());
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freeze-location", freezeLoc == null ? "null" : freezeLoc.toString());
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".mysql", false);
        this.plugin.getPlayerConfig().saveConfig();
        this.plugin.getPlayerConfig().reloadConfig();
        FrozenPlayer frozenPlayer = new FrozenPlayer(freezeDate, freezeeUUID, freezeeName, freezerName, originalLoc, freezeLoc, false, helmet);
        this.playerManager.addFrozenPlayer(freezeeUUID, frozenPlayer);
    }

    public void tempFreeze(final UUID freezeeUUID, final String freezeeName, String freezerName, String location, long time) {
        ItemStack helmet = null;
        SFLocation originalLoc = null;
        SFLocation freezeLoc = null;
        final Player onlineFreezee = Bukkit.getPlayer(freezeeUUID);
        String playerPlaceholder = freezeeName;
        String freezerPlaceholder = freezerName;
        String locationPlaceholder = location == null ? this.plugin.getConfig().getString("location") : this.plugin.getConfig().getString("locations." + location + ".placeholder", location);
        String timePlaceholder = TimeUtil.formatTime(time);
        String serversPlaceholder = "";

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
            } else if (freezeLoc == null) {
                freezeLoc = new SFLocation(originalLoc.clone());
                if (this.plugin.getConfig().getBoolean("enable-fly")) {
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
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freezee-name", freezeeName);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freezer-name", freezerName);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".orginal-location", originalLoc == null ? "null" : originalLoc.toString());
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freeze-location", freezeLoc == null ? "null" : freezeLoc.toString());
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".mysql", false);
        this.plugin.getPlayerConfig().saveConfig();
        this.plugin.getPlayerConfig().reloadConfig();
        final TempFrozenPlayer tempFrozenPlayer = new TempFrozenPlayer(freezeDate, unfreezeDate, freezeeUUID, freezeeName, freezerName, originalLoc, freezeLoc, false, helmet);
        BukkitTask task;
        if (!tempFrozenPlayer.isSqlFreeze()) {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    Player p = Bukkit.getPlayer(freezeeUUID);
                    if (p != null) {
                        for (String msg : plugin.getConfig().getStringList("unfreeze-message")) {
                            p.sendMessage(plugin.placeholders(msg).replace("{PLAYER}", freezeeName));
                        }
                        p.getInventory().setHelmet(tempFrozenPlayer.getHelmet());
                        p.teleport(tempFrozenPlayer.getOriginalLoc());
                    }
                    plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString(), null);
                    plugin.getPlayerConfig().saveConfig();
                    plugin.getPlayerConfig().reloadConfig();
                    playerManager.removeFrozenPlayer(freezeeUUID);

                }
            }.runTaskLater(this.plugin, time * 20L);
        } else {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    //SQL TABLE STUFF
                }
            }.runTaskLater(this.plugin, time * 20L);
        }
        tempFrozenPlayer.setTask(task);
        FrozenPlayer frozenPlayer = (FrozenPlayer) tempFrozenPlayer;
        this.playerManager.addFrozenPlayer(freezeeUUID, frozenPlayer);
    }

    public void notifyOfFreeze(CommandSender sender, UUID uuid, String location) {
        FrozenPlayer frozenPlayer = this.playerManager.getFrozenPlayer(uuid);
        Player onlineFreezee = Bukkit.getPlayer(uuid);
        String playerPlaceholder = frozenPlayer.getFreezeeName();
        String freezerPlaceholder = frozenPlayer.getFreezerName();
        String timePlaceholder = "";
        String serversPlaceholder = "";
        String locationPlaceholder = location == null ? this.plugin.getConfig().getString("location") : this.plugin.getConfig().getString("locations." + location + ".placeholder", location);
        String playerMessage;
        String notifyMessage;
        if (frozenPlayer instanceof TempFrozenPlayer) {
            timePlaceholder = TimeUtil.formatTime((((TempFrozenPlayer) frozenPlayer).getUnfreezeDate() - System.currentTimeMillis()) / 1000L);
            playerMessage = "temp-freeze-message";
            if (location != null) {
                playerMessage = "temp-freeze-location-message";
            }
            notifyMessage = "temp-frozen-notify-message";
            if (frozenPlayer.isSqlFreeze()) {
                notifyMessage = "sql-temp-frozen-notify-message";
            }
        } else {
            playerMessage = "freeze-message";
            if (location != null) {
                playerMessage = "freeze-location-message";
            }
            notifyMessage = "frozen-notify-message";
            if (frozenPlayer.isSqlFreeze()) {
                notifyMessage = "sql-frozen-notify-message";
            }
        }

        if (onlineFreezee != null) {
            for (String msg : this.plugin.getConfig().getStringList(playerMessage)) {
                if (!msg.equals("")) {
                    onlineFreezee.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerPlaceholder).replace("{PLAYER}", playerPlaceholder).replace("{TIME}", timePlaceholder)
                            .replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
                }
            }
        }
        for (String msg : this.plugin.getConfig().getStringList(notifyMessage)) {
            if (!msg.equals("")) {
                sender.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerPlaceholder).replace("{PLAYER}", playerPlaceholder).replace("{TIME}", timePlaceholder)
                        .replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
            }
        }
        Player senderP = sender instanceof Player ? (Player) sender : null;
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.hasPermission("sf.notify.freeze") && p != senderP) {
                for (String msg : this.plugin.getConfig().getStringList(notifyMessage)) {
                    if (!msg.equals("")) {
                        sender.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerPlaceholder).replace("{PLAYER}", playerPlaceholder).replace("{TIME}", timePlaceholder)
                                .replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
                    }
                }
            }
        }
    }

    public void notifyOfUnfreeze(CommandSender sender, UUID uuid) {
        FrozenPlayer frozenPlayer = this.playerManager.getFrozenPlayer(uuid);
        Player onlineFreezee = Bukkit.getPlayer(uuid);
        String playerPlaceholder = frozenPlayer.getFreezeeName();
        String unfreezerPlaceholder = sender.getName();

        if (onlineFreezee != null) {
            for (String msg : this.plugin.getConfig().getStringList("unfreeze-message")) {
                if (!msg.equals("")) {
                    onlineFreezee.sendMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerPlaceholder).replace("{PLAYER}", playerPlaceholder)));
                }
            }
        }

        for (String msg : this.plugin.getConfig().getStringList("unfrozen-notify-message")) {
            if (!msg.equals("")) {
                sender.sendMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerPlaceholder).replace("{PLAYER}", playerPlaceholder)));
            }
        }
        Player senderP = sender instanceof Player ? (Player) sender : null;
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.hasPermission("sf.notify.unfreeze") && p != senderP) {
                for (String msg : this.plugin.getConfig().getStringList("unfrozen-notify-message")) {
                    if (!msg.equals("")) {
                        sender.sendMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerPlaceholder).replace("{PLAYER}", playerPlaceholder)));
                    }
                }
            }
        }
    }

}
