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

import java.util.UUID;

public class FreezeCommand implements CommandExecutor {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;
    private final FreezeManager freezeManager;
    private final LocationManager locationManager;
    private final Permission permissions;

    public FreezeCommand(SimpleFreezeMain plugin, PlayerManager playerManager, FreezeManager freezeManager, LocationManager locationManager, Permission permissions) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.freezeManager = freezeManager;
        this.locationManager = locationManager;
        this.permissions = permissions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("freeze")) {

            if (!sender.hasPermission("sf.freeze")) {
                for (String msg : this.plugin.getConfig().getStringList("no-permission-message")) {
                    if (!msg.equals("")) {
                        sender.sendMessage(this.plugin.placeholders(msg));
                    }
                }
                return false;
            }

            if (args.length < 1) {
                sender.sendMessage(this.plugin.placeholders("{PREFIX}" + "Not enough arguments, try &b/freeze <name> [location] [servers]"));
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
                    sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("exempt-messages.freeze").replace("{PLAYER}", playerName)));
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
                    } else if (this.permissions.playerHas(null, offlineP, "sf.exempt.*") || this.permissions.playerHas(null, offlineP, "sf.exempt.freeze")) {
                        sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("exempt-messages.freeze").replace("{PLAYER}", playerName)));
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

            //this.plugin.getPlayerConfig().getConfig().set("players." + uuid.toString() + ".unfreeze-date", null);

            if (args.length == 1) {
                this.freezeManager.freeze(uuid, sender instanceof Player ? ((Player) sender).getUniqueId() : null, null, null);
                if (onlineP == null) {
                    this.freezeManager.notifyOfFreeze(uuid);
                } else {
                    this.freezeManager.notifyOfFreeze(this.playerManager.getFrozenPlayer(uuid));
                }
                return true;
            }

            if (args.length > 1) {
                String location = null;
                String reason = null;
                if (!this.plugin.getConfig().isSet("locations." + args[1])) {
                    if (args.length == 2) {
                        location = args[1];
                        sender.sendMessage(this.plugin.placeholders("{PREFIX}&b" + location + " &7is not a valid location, try:"));
                        String locations = "";
                        for (String locationName : this.plugin.getConfig().getConfigurationSection("locations").getKeys(false)) {
                            locations += "&b" + locationName + this.plugin.getFinalPrefixFormatting() + ", ";
                        }
                        sender.sendMessage(this.plugin.placeholders(locations.substring(0, locations.length() - 2)));
                        return false;
                    } else {
                        reason = "";
                        for (int i = 1; i < args.length; i++) {
                            reason += args[i] + " ";
                        }
                        reason = reason.substring(0, reason.length() - 1);
                    }
                }
                this.freezeManager.freeze(uuid, sender instanceof Player ? ((Player) sender).getUniqueId() : null, location, reason);
                if (onlineP == null) {
                    this.freezeManager.notifyOfFreeze(uuid);
                } else {
                    this.freezeManager.notifyOfFreeze(this.playerManager.getFrozenPlayer(uuid));
                }
                return true;
            }

        }

        return false;
    }

}
