package org.plugins.simplefreeze.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class LocationsConfig {

    private JavaPlugin plugin;
    private FileConfiguration locationsConfig = null;
    private File locationsConfigFile = null;

    public LocationsConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reloadConfig() {
        if (this.locationsConfigFile == null) {
            this.locationsConfigFile = new File(this.plugin.getDataFolder(), "locations.yml");
        }
        this.locationsConfig = YamlConfiguration.loadConfiguration(this.locationsConfigFile);

        // Look for defaults in the jar
        try {
            Reader defConfigStream = new InputStreamReader(this.plugin.getResource("locations.yml"), "UTF8");
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                this.locationsConfig.setDefaults(defConfig);
                this.locationsConfig.options().copyDefaults(true);
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        if (this.locationsConfig == null) {
            reloadConfig();
        }
        return this.locationsConfig;
    }

    public void saveConfig() {
        if (this.locationsConfig == null || this.locationsConfigFile == null) {
            return;
        }
        try {
            getConfig().save(this.locationsConfigFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void saveDefaultConfig() {
        if (this.locationsConfigFile == null) {
            this.locationsConfigFile = new File(this.plugin.getDataFolder(), "locations.yml");
        }
        if (!this.locationsConfigFile.exists()) {
            this.plugin.saveResource("locations.yml", false);
        }
    }

}
