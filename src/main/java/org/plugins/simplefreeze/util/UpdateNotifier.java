package org.plugins.simplefreeze.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateNotifier {

    private final JavaPlugin javaPlugin;
    private final String localPluginVersion;
    private String spigotPluginVersion;
    private boolean needsUpdate = false;


    //Constants. Customize to your liking.
    private static final int ID = 18044; //The ID of your resource. Can be found in the resource URL.
    private static final String ERR_MSG = "&cUpdate checker failed!";
    private static String UPDATE_MSG = "&fA new update is available at:&b https://www.spigotmc.org/resources/" + ID + "/updates";

    public UpdateNotifier(final JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
        this.localPluginVersion = javaPlugin.getDescription().getVersion();
    }

    public void checkForUpdate() {
        //The request is executed asynchronously as to not block the main thread.
        Bukkit.getScheduler().runTaskAsynchronously(this.javaPlugin, () -> {
            //Request the current version of your plugin on SpigotMC.
            try {
                HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=" + ID).openConnection();
                connection.setRequestMethod("GET");
                this.spigotPluginVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
                int spaceIndex = this.spigotPluginVersion.indexOf(" ");
                if (spaceIndex > -1) {
                    this.spigotPluginVersion = this.spigotPluginVersion.substring(0, spaceIndex);
                }
            } catch (IOException e) {
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ERR_MSG));
                e.printStackTrace();
                return;
            }

            //Check if the requested version is the same as the one in your plugin.yml.
            if (this.localPluginVersion.equals(this.spigotPluginVersion)) return;

            this.needsUpdate = true;
            UPDATE_MSG = "{PREFIX}You are still running version &b" + this.localPluginVersion + "&7, latest version: &b" + this.spigotPluginVersion + "\n{PREFIX}https://www.spigotmc.org/resources/" + ID + "/";
        });
    }

    public boolean needsUpdate() {
        return needsUpdate;
    }

    public static String getUpdateMsg() {
        return UPDATE_MSG;
    }
}