package org.plugins.simplefreeze;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.plugins.simplefreeze.commands.*;
import org.plugins.simplefreeze.listeners.*;
import org.plugins.simplefreeze.managers.FreezeManager;
import org.plugins.simplefreeze.managers.PlayerManager;
import org.plugins.simplefreeze.managers.SQLManager;
import org.plugins.simplefreeze.util.PlayersConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/* 
 * TODO: 
 *  - https://bukkit.org/threads/github-and-eclipse.40351/
 *  - Add lore to head-block
 *  - Add Time
 *  - Remember when freezeall is active after crash/restart
 *  - Fix bug where u cannot unfreeze a player if u provide a invalid location
 *  - Add option to spawn block below player if they are in the ground or make it so they cannot be kicked for fly/stop anticheat spam
 *  - Titles
 *  - Actionbar
 *  
 *  TODO: Hopefully
 *  - Hologram above players head on freeze option
 * 
 * BUGS:
 *  - Fix ice on head when inventory is full (helmet needs to be stored)
 * */

/* CHANGES:
 *   - ALLOW OFFLINE FREEZING/UNFREEZING
 *   - PLAYERS MAY NOW EDIT THEIR INVENTORY WHILE FROZEN
 *   - SQL FREEZING
 *   - TEMPORARY FREEZING
 *   - HELMET HAS MORE OPTIONS, CAN BE ANY ITEM/BLOCK, PLACEHOLDERS, ITEMFLAGS, ENCHANTS, LORE, NAME
 *   - /SF RELOAD NOW UPDATES HELMETS AND PARTICLES IF THEY ARE CHANGED
 * 
 * BUGS:
 *   - FIXED BUG WHERE PLAYERS WERE SOMETIMES TELEPORTED INTO SUFFICATION THROUGH THE TELEPORT-UP OPTION
 * 
 * */

/* NMS:
 * net.minecraft.server.v1_7_R4
 * net.minecraft.server.v1_8_R1
 * net.minecraft.server.v1_8_R2
 * net.minecraft.server.v1_8_R3
 * net.minecraft.server.v1_9_R1
 * net.minecraft.server.v1_9_R2
 * net.minecraft.server.v1_10_R1
 * */

public class SimpleFreezeMain extends JavaPlugin {

	private String finalPrefixFormatting = this.updateFinalPrefixFormatting();

	private final PlayerManager playerManager = new PlayerManager();

	private PlayersConfig playersConfig;
	private SQLManager sqlManager;
	private FreezeManager freezeManager;

	@Override
	public void onEnable() {
		this.initializeVariables();
		this.loadConfigs();
		this.registerCommands();
		this.registerListeners();
	}

	@Override
	public void onDisable() {

	}

	private void initializeVariables() {
		this.playersConfig = new PlayersConfig(this);
		this.sqlManager = new SQLManager(this);
		this.freezeManager = new FreezeManager(this, this.playerManager, this.sqlManager);
	}

	private void loadConfigs() {
		this.getConfig().options().copyDefaults(true);
		this.saveDefaultConfig();
		this.reloadConfig();

		this.playersConfig.getConfig().options().copyDefaults(true);
		this.playersConfig.saveDefaultConfig();
		this.playersConfig.reloadConfig();
	}

	private void registerCommands() {
		this.getCommand("simplefreeze").setExecutor(new SimpleFreezeCommand(this, this.freezeManager));
		this.getCommand("freeze").setExecutor(new FreezeCommand(this, this.playerManager, this.freezeManager));
		this.getCommand("tempfreeze").setExecutor(new TempFreezeCommand(this, this.playerManager, this.freezeManager));
		this.getCommand("unfreeze").setExecutor(new UnfreezeCommand(this, this.playerManager, this.freezeManager));
		this.getCommand("frozen").setExecutor(new FrozenCommand());
	}

	private void registerListeners() {
		PluginManager plManager = this.getServer().getPluginManager();
		plManager.registerEvents(new EntityCombustListener(this, this.playerManager), this);
		plManager.registerEvents(new EntityDamageEntityListener(this, this.playerManager), this);
		plManager.registerEvents(new EntityDamageListener(this, this.playerManager), this);
		plManager.registerEvents(new InventoryClickListener(this, this.playerManager), this);
		plManager.registerEvents(new PlayerChatListener(this, this.playerManager), this);
		plManager.registerEvents(new PlayerDropListener(this, this.playerManager), this);
		plManager.registerEvents(new PlayerEnderpearlListener(this, this.playerManager), this);
		plManager.registerEvents(new PlayerInteractListener(this, this.playerManager), this);
		plManager.registerEvents(new PlayerJoinListener(this, this.playerManager), this);
		plManager.registerEvents(new PlayerMoveListener(this, this.playerManager), this);
		plManager.registerEvents(new PlayerQuitListener(this, this.playerManager), this);
	}

	public PlayersConfig getPlayerConfig() {
		return this.playersConfig;
	}

	public String placeholders(String arg) {
		return StringEscapeUtils.unescapeJava(ChatColor.translateAlternateColorCodes('&', arg.replace("{PREFIX}", this.getConfig().getString("prefix"))));
	}

	public String getHelpMessage() {
		return this.placeholders("                                           &b&lSimpleFreeze\n" + "&b/sf &8- &7Displays this message\n" + "&b/sf reload &8- &7Reloads configuration file\n" + "&b/frozen &8- &7Lists frozen players\n"
				+ "&b/freeze <name> [location] [servers] &8- &7Freezes a player\n" + "&b/tempfreeze <name> <time> [servers] &8- &7Temporarily freezes a player\n" + "&b/unfreeze <name> &8- &7Unfreezes a player\n"
				+ "&b/freezeall &8- &7Freeze all players\n");
	}

	public String getFinalPrefixFormatting() {
		return this.finalPrefixFormatting;
	}

	public String updateFinalPrefixFormatting() {
		String color = "", format = "";
		String prefix = this.placeholders("{PREFIX}");
		if (prefix.length() > 1) {
			for (int index = prefix.length(); index > 1; index--) {
				String bit = prefix.substring(index - 2, index);
				if (bit.startsWith("�")) {
					int chNum = (int) bit.toLowerCase().charAt(1);
					if ((97 <= chNum && chNum <= 102) || (48 <= chNum && chNum <= 57) || chNum == 114) {
						color = bit;
						break;
					}
					if (107 <= chNum && chNum <= 112) {
						format += bit;
					}

				}
			}
		}
		this.finalPrefixFormatting = color + format;
		return color + format;
	}

	public String getLatestVersion() {
		try {
			HttpURLConnection con = (HttpURLConnection) new URL("http://www.spigotmc.org/api/general.php").openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.getOutputStream().write(("key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource=" + "18044").getBytes("UTF-8"));
			String version = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
			
			if (version.contains(" ")) {
				return version.substring(0, version.indexOf(" "));
			} else {
				return version;
			}
		} catch (Exception ex) {
			this.getLogger().info("Failed to check for a update on spigot.");
			return "";
		}
	}

}
