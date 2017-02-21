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
import org.plugins.simplefreeze.managers.PlayerManager;
import org.plugins.simplefreeze.util.TimeUtil;

import java.util.UUID;

public class TempFreezeCommand implements CommandExecutor {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;
    private final FreezeManager freezeManager;
    private final Permission permission;

    public TempFreezeCommand(SimpleFreezeMain plugin, PlayerManager playerManager, FreezeManager freezeManager, Permission permission) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.freezeManager = freezeManager;
        this.permission = permission;
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
                sender.sendMessage(this.plugin.placeholders("{PREFIX}" + "Not enough arguments, try &b/tempfreeze <name> <time> [location] [servers]"));
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
            } else if (offlineP != null) {
                if (offlineP.hasPlayedBefore()) {
                    playerName = offlineP.getName();
                    uuid = offlineP.getUniqueId();
                    if (!sender.hasPermission("sf.offline")) {
                        for (String msg : this.plugin.getConfig().getStringList("no-permission-message")) {
                            if (!msg.equals("")) {
                                sender.sendMessage(this.plugin.placeholders(msg));
                            }
                        }
                        return true;
                    } else if (this.permission.playerHas(null, offlineP, "sf.exempt.*") || this.permission.playerHas(null, offlineP, "sf.exempt.tempfreeze")) {
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

            long time = TimeUtil.convertToSeconds(args[1]);

            if (args.length == 2) {
                this.freezeManager.tempFreeze(uuid, sender instanceof Player ? ((Player) sender).getUniqueId() : null, null, time);
                if (onlineP == null) {
                    this.freezeManager.notifyOfFreeze(uuid);
                } else {
                    this.freezeManager.notifyOfFreeze(this.playerManager.getFrozenPlayer(uuid));
                }
                return true;
            }

            if (args.length > 2) {
                String location = args[2];
                if (!this.plugin.getConfig().isSet("locations." + location)) {
                    sender.sendMessage(this.plugin.placeholders("{PREFIX}&b" + location + " &7is not a valid location, try:"));
                    String locations = "";
                    for (String locationName : this.plugin.getConfig().getConfigurationSection("locations").getKeys(false)) {
                        locations += "&b" + locationName + this.plugin.getFinalPrefixFormatting() + ", ";
                    }
                    sender.sendMessage(this.plugin.placeholders(locations.substring(0, locations.length() - 2)));
                    return false;
                }
                this.freezeManager.tempFreeze(uuid, sender instanceof Player ? ((Player) sender).getUniqueId() : null, location, time);
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