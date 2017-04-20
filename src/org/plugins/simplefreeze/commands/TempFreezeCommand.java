package org.plugins.simplefreeze.commands;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.FreezeManager;
import org.plugins.simplefreeze.managers.LocationManager;
import org.plugins.simplefreeze.managers.PlayerManager;
import org.plugins.simplefreeze.managers.SQLManager;
import org.plugins.simplefreeze.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TempFreezeCommand implements CommandExecutor {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;
    private final FreezeManager freezeManager;
    private final LocationManager locationManager;
    private final SQLManager sqlManager;
    private final Permission permissions;

    public TempFreezeCommand(SimpleFreezeMain plugin, PlayerManager playerManager, FreezeManager freezeManager, LocationManager locationManager, SQLManager sqlManager, Permission permissions) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.freezeManager = freezeManager;
        this.locationManager = locationManager;
        this.sqlManager = sqlManager;
        this.permissions = permissions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("tempfreeze")) {

            if (!sender.hasPermission("sf.tempfreeze")) {
                for (String msg : this.plugin.getConfig().getStringList("no-permission-message")) {
                    if (!msg.equals("")) {
                        sender.sendMessage(this.plugin.placeholders(msg));
                    }
                }
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(this.plugin.placeholders("{PREFIX}" + "Not enough arguments, try &b/tempfreeze <name> <time> [location/servers]"));
                return true;
            }

            String playerName = "";
            UUID uuid = null;
            Player onlineP = Bukkit.getPlayer(args[0]);
            OfflinePlayer offlineP = Bukkit.getOfflinePlayer(args[0]);

            if (onlineP != null) {
                playerName = onlineP.getName();
                uuid = onlineP.getUniqueId();
                if (onlineP.hasPermission("sf.exempt.*") || onlineP.hasPermission("sf.exempt.freeze")) {
                    sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("exempt-messages.tempfreeze").replace("{PLAYER}", playerName)));
                    return true;
                }
                // CHECK IF MAX DISTANCE IS EXCEEDED
                if (sender instanceof Player && this.plugin.getConfig().getInt("freeze-radius") > 0) {
                    int maxDistance = this.plugin.getConfig().getInt("freeze-radius");
                    int totalDistance = this.locationManager.getTotalDistance((Player) sender, onlineP);
                    int distanceDifference = totalDistance - maxDistance;
                    if (!sender.hasPermission("sf.exempt.distance") && distanceDifference > 0) {
                        for (String msg : this.plugin.getConfig().getStringList("freeze-distance-fail")) {
                            sender.sendMessage(this.plugin.placeholders(msg.replace("{PLAYER}", onlineP.getName()).replace("{MAXDISTANCE}", "" + maxDistance).replace("{TOTALDISTANCE}", "" + totalDistance).replace("{DISTANCEDIFFERENCE}", "" + distanceDifference)));
                        }
                        return true;
                    }
                }
            } else if (offlineP != null) {
                if (offlineP.hasPlayedBefore()) {
                    if (this.permissions == null) {
                        sender.sendMessage(this.plugin.placeholders("{PREFIX}You can't freeze offline players without &bVault {PREFIXFORMAT}enabled"));
                        return false;
                    }
                    playerName = offlineP.getName();
                    uuid = offlineP.getUniqueId();
                    if (!sender.hasPermission("sf.offline")) {
                        for (String msg : this.plugin.getConfig().getStringList("no-permission-offline-player-message")) {
                            if (!msg.equals("")) {
                                sender.sendMessage(this.plugin.placeholders(msg));
                            }
                        }
                        return true;
                    } else if (this.permissions.playerHas(null, offlineP, "sf.exempt.*") || this.permissions.playerHas(null, offlineP, "sf.exempt.tempfreeze")) {
                        sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("exempt-messages.tempfreeze").replace("{PLAYER}", playerName)));
                        return true;
                    }
                } else {
                    sender.sendMessage(this.plugin.placeholders("{PREFIX}&b{PLAYER} " + this.plugin.getFinalPrefixFormatting() + "has never played this server before").replace("{PLAYER}", args[0]));
                    return true;
                }
            } else {
                sender.sendMessage(this.plugin.placeholders("{PREFIX}&b{PLAYER} " + this.plugin.getFinalPrefixFormatting() + "has never played this server before").replace("{PLAYER}", args[0]));
                return true;
            }

            if (this.playerManager.isFrozen(uuid)) {
                if (!this.playerManager.isFreezeAllFrozen(uuid)) {
                    UUID freezerUUID = this.plugin.getPlayerConfig().getConfig().getString("players." + uuid.toString() + ".freezer-uuid", "null").equals("null") ? null : UUID.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + uuid.toString() + ".freezer-uuid"));
                    String freezerName = freezerUUID == null ? "CONSOLE" : Bukkit.getPlayer(freezerUUID) == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer(freezerUUID).getName();
                    for (String msg : this.plugin.getConfig().getStringList("already-frozen")) {
                        if (!msg.equals("")) {
                            sender.sendMessage(this.plugin.placeholders(msg.replace("{PLAYER}", playerName).replace("{FREEZER}", freezerName)));
                        }
                    }
                    return true;
                }
            }

            UUID senderUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
            String location = null;
            String reason = null;
            String timeString = "";
            List<String> serverIDs = this.sqlManager.getServerIDs();
            List<String> servers = new ArrayList<>();
            boolean calculatingTime = true;
            for (int i = 1; i < args.length; i++) {
                if (calculatingTime) {
                    if (TimeUtil.isUnitOfTime(args[i])) {
                        timeString += args[i];
                    } else {
                        calculatingTime = false;
                    }
                }
                if (!calculatingTime) {
                    if (reason != null) {
                        reason += " " + args[i];
                    } else {
                        boolean addedServer = false;
                        if (location == null) {
                            for (String serverID : serverIDs) {
                                if (serverID.equalsIgnoreCase(args[i])) {
                                    servers.add(serverID);
                                    addedServer = true;
                                    break;
                                }
                            }
                        }
                        boolean addedLocation = false;
                        if (servers.isEmpty() && this.plugin.getLocationsConfig().getConfig().isSet("locations." + args[i].toLowerCase())) {
                            location = args[i].toLowerCase();
                            addedLocation = true;
                        }
                        if (!addedServer && !addedLocation) {
                            reason = args[i];
                        }
                    }
                }
            }

            long time = TimeUtil.convertToSeconds(timeString);
            if (time == -1) {
                sender.sendMessage(this.plugin.placeholders("{PREFIX}&b" + args[1] + " {PREFIXFORMAT}is not a valid unit of time, try &bs&7, &bm&7, &bh&7, &bd&7, &bw&7, &bmo&7 or &by&7"));
                return false;
            }

            if (location == null && this.locationManager.getLocation(this.plugin.getConfig().getString("default-location", "")) != null) {
                location = this.plugin.getConfig().getString("default-location");
            }

            if (reason == null) {
                reason = this.plugin.getConfig().getString("default-reason");
            }

            if (!servers.isEmpty()) {
                if (!sender.hasPermission("sf.mysql")) {
                    sender.sendMessage(this.plugin.placeholders("{PREFIX}You don't have permission to freeze players on other/multiple servers"));
                    return false;
                }
                this.sqlManager.addFreeze(playerName, uuid, sender.getName(), senderUUID, System.currentTimeMillis() + (time * 1000L), reason, servers);
                return true;
            }
            this.freezeManager.tempFreeze(uuid, senderUUID, location, time, reason, null);

            if (onlineP == null) {
                this.freezeManager.notifyOfFreeze(uuid);
            } else {
                this.freezeManager.notifyOfFreeze(this.playerManager.getFrozenPlayer(uuid));
            }
            return true;
        }

        return false;
    }

}