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

import java.util.UUID;

public class FreezeCommand implements CommandExecutor {

	private final SimpleFreezeMain plugin;
	private final PlayerManager playerManager;
	private final FreezeManager freezeManager;
	
	public FreezeCommand(SimpleFreezeMain plugin, PlayerManager playerManager, FreezeManager freezeManager) {
		this.plugin = plugin;
		this.playerManager = playerManager;
		this.freezeManager = freezeManager;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (cmd.getName().equalsIgnoreCase("freeze")) {
			
			if (!sender.hasPermission("sf.freeze")) {
				sender.sendMessage(this.plugin.getConfig().getString("no-permission-message"));
				return true;
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
			
			if (args.length == 1) {
				this.freezeManager.freeze(uuid, playerName, sender.getName(), null);
				this.freezeManager.notifyOfFreeze(sender, uuid, null);
				return true;
			}
			
			if (args.length > 1) {
				String location = args[1];
				if (!this.plugin.getConfig().isSet("locations." + location)) {
					sender.sendMessage(this.plugin.placeholders("{PREFIX}&b" + location + " &7is not a valid location, try:"));
					String locations = "";
					for (String locationName : this.plugin.getConfig().getConfigurationSection("locations").getKeys(false)) {
						locations += "&b" + locationName + this.plugin.getFinalPrefixFormatting() + ", ";
					}
					sender.sendMessage(this.plugin.placeholders(locations.substring(0, locations.length() - 2)));
					return false;
				}
				this.freezeManager.freeze(uuid, playerName, sender.getName(), location);
				this.freezeManager.notifyOfFreeze(sender, uuid, location);
				return true;
			}
			
		}
		
		return false;
	}

}
