package org.plugins.simplefreeze.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.FreezeManager;
import org.plugins.simplefreeze.managers.PlayerManager;
import org.plugins.simplefreeze.managers.SQLManager;

import java.util.List;
import java.util.UUID;

public class UnfreezeCommand implements CommandExecutor {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;
    private final FreezeManager freezeManager;
    private final SQLManager sqlManager;

    public UnfreezeCommand(SimpleFreezeMain plugin, PlayerManager playerManager, FreezeManager freezeManager, SQLManager sqlManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.freezeManager = freezeManager;
        this.sqlManager = sqlManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("unfreeze")) {

            if (!sender.hasPermission("sf.unfreeze")) {
                for (String msg : this.plugin.getConfig().getStringList("no-permission-message")) {
                    if (!msg.equals("")) {
                        sender.sendMessage(this.plugin.placeholders(msg));
                    }
                }
                return true;
            }

            if (args.length < 1) {
                sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("not-enough-arguments.unfreeze")));
                return true;
            }

            String playerName;
            UUID uuid;
            Player onlineP = Bukkit.getPlayer(args[0]);
            OfflinePlayer offlineP = onlineP == null ? Bukkit.getOfflinePlayer(args[0]) : null;

            if (onlineP != null) {
                playerName = onlineP.getName();
                uuid = onlineP.getUniqueId();
            } else if (offlineP != null) {
                if (offlineP.hasPlayedBefore()) {
                    playerName = offlineP.getName();
                    uuid = offlineP.getUniqueId();
                } else {
                    for (String line : this.plugin.getConfig().getStringList("never-played-before")) {
                        sender.sendMessage(this.plugin.placeholders(line).replace("{PLAYER}", args[0]));
                    }
                    return true;
                }
            } else {
                for (String line : this.plugin.getConfig().getStringList("never-played-before")) {
                    sender.sendMessage(this.plugin.placeholders(line).replace("{PLAYER}", args[0]));
                }
                return true;
            }

            sender.sendMessage(playerName + " is frozen ? " + this.playerManager.isFrozen(uuid));

            if (!this.playerManager.isFrozen(uuid)) {
                if (!this.plugin.usingMySQL() || !this.sqlManager.checkIfFrozen(uuid)) {
                    for (String msg : this.plugin.getConfig().getStringList("not-frozen")) {
                        if (!msg.equals("")) {
                            sender.sendMessage(this.plugin.placeholders(msg.replace("{PLAYER}", playerName)));
                        }
                    }
                    return true;
                }
            }

            List<String> frozenServers = this.sqlManager.getFrozenServers(uuid);

            if (!this.playerManager.isFreezeAllFrozen(uuid) && this.plugin.usingMySQL() && this.sqlManager.checkIfFrozen(uuid)) {
                if (sender.hasPermission("sf.mysql")) {
                    this.sqlManager.addUnfreeze(playerName, uuid, sender.getName());

                    if (!frozenServers.contains(this.plugin.getServerID().toLowerCase())) {
                        String unfreezerName = sender.getName();
                        if (!(sender instanceof Player)) {
                            unfreezerName = SimpleFreezeMain.getConsoleName();
                        }
                        String playerPath = "unfreeze-message";
                        String notifyPath = "unfrozen-notify-message";

                        if (unfreezerName.equals(SimpleFreezeMain.getConsoleName())) {
                            for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
                                Bukkit.getConsoleSender().sendMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerName).replace("{PLAYER}", playerName).replace("{SERVER}", this.plugin.getServerID())));
                            }
                        }

                        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                            if (p.hasPermission("sf.notify.unfreeze")) {
                                for (String msg : this.plugin.getConfig().getStringList(notifyPath)) {
                                    p.sendMessage(this.plugin.placeholders(msg.replace("{UNFREEZER}", unfreezerName).replace("{PLAYER}", playerName).replace("{SERVER}", this.plugin.getServerID())));
                                }
                            }
                        }
                    }
                    return true;
                }
            }

            this.freezeManager.notifyOfUnfreeze(sender, uuid, playerName, this.plugin.getServerID());
            this.freezeManager.unfreeze(uuid);

            return true;
        }

        return false;
    }

}
