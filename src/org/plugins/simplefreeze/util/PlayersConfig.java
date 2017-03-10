package org.plugins.simplefreeze.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class PlayersConfig {

	private JavaPlugin plugin;
	private FileConfiguration playerDataConfig = null;
	private File playerDataConfigFile = null;

	public PlayersConfig(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	public void reloadConfig() {
		if (this.playerDataConfigFile == null) {
			this.playerDataConfigFile = new File(this.plugin.getDataFolder(), "playerdata.yml");
		}
		this.playerDataConfig = YamlConfiguration.loadConfiguration(this.playerDataConfigFile);

		// Look for defaults in the jar
		try {
			Reader defConfigStream = new InputStreamReader(this.plugin.getResource("playerdata.yml"), "UTF8");
			if (defConfigStream != null) {
				YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
				this.playerDataConfig.setDefaults(defConfig);
				this.playerDataConfig.options().copyDefaults(true);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public FileConfiguration getConfig() {
		if (this.playerDataConfig == null) {
			reloadConfig();
		}
		return this.playerDataConfig;
	}

	public void saveConfig() {
		if (this.playerDataConfig == null || this.playerDataConfigFile == null) {
			return;
		}
		try {
			getConfig().save(this.playerDataConfigFile);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void saveDefaultConfig() {
		if (this.playerDataConfigFile == null) {
			this.playerDataConfigFile = new File(this.plugin.getDataFolder(), "playerdata.yml");
		}
		if (!this.playerDataConfigFile.exists()) {
			this.plugin.saveResource("playerdata.yml", false);
		}
	}
	
}
