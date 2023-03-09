package org.plugins.simplefreeze.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class StatsConfig {

    private JavaPlugin plugin;
    private FileConfiguration statsConfig = null;
    private File statsConfigFile = null;

    public StatsConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reloadConfig() {
        if (this.statsConfigFile == null) {
            this.statsConfigFile = new File(this.plugin.getDataFolder(), "stats.yml");
        }
        this.statsConfig = YamlConfiguration.loadConfiguration(this.statsConfigFile);

        // Look for defaults in the jar
        try {
            Reader defConfigStream = new InputStreamReader(this.plugin.getResource("stats.yml"), "UTF8");
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                this.statsConfig.setDefaults(defConfig);
                this.statsConfig.options().copyDefaults(true);
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        if (this.statsConfig == null) {
            reloadConfig();
        }
        return this.statsConfig;
    }

    public void saveConfig() {
        if (this.statsConfig == null || this.statsConfigFile == null) {
            return;
        }
        try {
            getConfig().save(this.statsConfigFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void saveDefaultConfig() {
        if (this.statsConfigFile == null) {
            this.statsConfigFile = new File(this.plugin.getDataFolder(), "stats.yml");
        }
        if (!this.statsConfigFile.exists()) {
            this.plugin.saveResource("stats.yml", false);
        }
    }

}
