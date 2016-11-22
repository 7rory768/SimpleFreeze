package org.plugins.simplefreeze.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.plugins.simplefreeze.SimpleFreezeMain;

public class PlayersConfig {

	private SimpleFreezeMain plugin;
	private FileConfiguration playerDataConfig = null;
	private File playerDataConfigFile = null;

	public PlayersConfig(SimpleFreezeMain plugin) {
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
