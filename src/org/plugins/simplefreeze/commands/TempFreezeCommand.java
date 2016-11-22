package org.plugins.simplefreeze.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

	public TempFreezeCommand(SimpleFreezeMain plugin, PlayerManager playerManager, FreezeManager freezeManager) {
		this.plugin = plugin;
		this.playerManager = playerManager;
		this.freezeManager = freezeManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (cmd.getName().equalsIgnoreCase("tempfreeze")) {

			if (!sender.hasPermission("sf.tempfreeze")) {
				sender.sendMessage(this.plugin.getConfig().getString("no-permission-message"));
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
			} else if (offlineP != null) {
				if (offlineP.hasPlayedBefore()) {
					playerName = offlineP.getName();
					uuid = offlineP.getUniqueId();
				} else {
					sender.sendMessage(this.plugin.placeholders("{PREFIX}&b{PLAYER} " + this.plugin.getFinalPrefixFormatting() + "has never played this server before").replace("{PLAYER}", args[0]));
					return true;
				}
			} else {
				sender.sendMessage(this.plugin.placeholders("{PREFIX}&b{PLAYER} " + this.plugin.getFinalPrefixFormatting() + "has never played this server before").replace("{PLAYER}", args[0]));
				return true;
			}

			if (this.playerManager.isFrozen(uuid)) {
				for (String msg : this.plugin.getConfig().getStringList("already-frozen")) {
					if (!msg.equals("")) {
						sender.sendMessage(this.plugin.placeholders(msg.replace("{PLAYER}", playerName).replace("{FREEZER}", this.playerManager.getFrozenPlayer(uuid).getFreezerName())));
					}
				}
				return true;
			}

			long time = TimeUtil.convertToSeconds(args[1]);

			if (args.length == 2) {
				this.freezeManager.tempFreeze(uuid, playerName, sender.getName(), null, time);
				this.freezeManager.notifyOfFreeze(sender, uuid, null);
				return true;
			}

			if (args.length > 2) {
				String location = args[2];
				if (!this.plugin.getConfig().isSet("locations." + location)) {
					sender.sendMessage(this.plugin.placeholders("{PREFIX}&b" + location + " &7is not a valid location, try:"));
					String locations = "";
					for (String locationName : this.plugin.getConfig().getConfigurationSection("locations").getKeys(false)) {
						locations += locationName + ", ";
					}
					sender.sendMessage(ChatColor.GRAY + locations.substring(0, locations.length() - 2));
					return false;
				}
				this.freezeManager.tempFreeze(uuid, playerName, sender.getName(), location, time);
				this.freezeManager.notifyOfFreeze(sender, uuid, location);
				return true;
			}

		}

		return false;
	}

}