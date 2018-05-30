package org.plugins.simplefreeze.cache;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.LocationManager;
import org.plugins.simplefreeze.objects.SFLocation;
import org.plugins.simplefreeze.util.TimeUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

/**
 * Created by Rory on 12/21/2016.
 */
public class FrozenPages {

    private final SimpleFreezeMain plugin;
    private final LocationManager locationManager;

    HashMap<UUID, String> playerInfo = new HashMap<UUID, String>();

    public FrozenPages(SimpleFreezeMain plugin, LocationManager locationManager) {
        this.plugin = plugin;
        this.locationManager = locationManager;
    }

    public void setupStrings() {
        for (String uuidStr : this.plugin.getPlayerConfig().getConfig().getConfigurationSection("players").getKeys(false)) {
            StringBuilder formatPath = new StringBuilder("frozen-list-format.formats.frozen");
            String playerPath = "players." + uuidStr + ".";

            UUID freezeeUUID = UUID.fromString(uuidStr);

            UUID freezerUUID = this.plugin.getPlayerConfig().getConfig().getString(playerPath + "freezer-uuid").equals("null") ? null : UUID.fromString(this.plugin.getPlayerConfig().getConfig().getString(playerPath + "freezer-uuid"));

            Player onlineFreezee = Bukkit.getPlayer(freezeeUUID);
            Player onlineFreezer = Bukkit.getPlayer(freezerUUID);

            Location freezeLocation = SFLocation.fromString(this.plugin.getPlayerConfig().getConfig().getString(playerPath + "freeze-location"));

            String freezeeName = onlineFreezee == null ? Bukkit.getOfflinePlayer(freezeeUUID).getName() : onlineFreezee.getName();

            if (freezeeName != null) {
                String freezerName = freezerUUID == null ? "CONSOLE" : onlineFreezer == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : onlineFreezer.getName();

                String onlinePlaceholder = onlineFreezee != null ? this.plugin.getConfig().getString("frozen-list-format.online-placeholder") : this.plugin.getConfig().getString("frozen-list-format.offline-placeholder");

                String timePlaceholder = this.plugin.getPlayerConfig().getConfig().isSet(playerPath + "unfreeze-date") ? TimeUtil.formatTime((this.plugin.getPlayerConfig().getConfig().getLong(playerPath + "unfreeze-date") - System.currentTimeMillis()) / 1000L) : "";

                if (!timePlaceholder.equals("")) {
                    if ((this.plugin.getPlayerConfig().getConfig().getLong(playerPath + "unfreeze-date") - System.currentTimeMillis()) / 1000L <= 0) {
                        continue;
                    }
                    formatPath = new StringBuilder("temp-frozen");
                }

                String locationName = this.locationManager.getLocationName(freezeLocation);
                if (locationName != null) {
                    formatPath.append("-location");
                }

                String locationPlaceholder = this.locationManager.getLocationPlaceholder(locationName);

                String reason = this.plugin.getPlayerConfig().getConfig().getString(playerPath + "reason", "");

                String servers = this.plugin.getPlayerConfig().getConfig().getString(playerPath + "servers", "");

                this.playerInfo.put(freezeeUUID, this.plugin.placeholders(this.plugin.getConfig().getString
                        (formatPath.toString()).replace("{ONLINE}", onlinePlaceholder).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{FREEZER}", freezerName).replace("{REASON}", reason).replace("{SERVERS}", servers)));
            }
        }
    }

    public void refreshString(UUID freezeeUUID) {
        //Bukkit.getLogger().info("[SF DEBUG] freezeeUUID = null? " + (freezeeUUID == null));

        String uuidStr = freezeeUUID.toString();
        StringBuilder path = new StringBuilder("frozen");

        UUID freezerUUID = this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freezer-uuid").equals("null") ? null : UUID.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freezer-uuid"));

        Location freezeLocation = SFLocation.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freeze-location"));

        //Bukkit.getLogger().info("[SF DEBUG] freezeLocation = null? " + (freezeLocation == null));

        String freezeeName = Bukkit.getPlayer(freezeeUUID) == null ? Bukkit.getOfflinePlayer(freezeeUUID).getName() : Bukkit.getPlayer(freezeeUUID).getName();


        //Bukkit.getLogger().info("[SF DEBUG] freezeeName = null? " + (freezeeName == null));

        String freezerName = freezerUUID == null ? this.plugin.getConfig().getString("console-name") : Bukkit.getPlayer(freezerUUID) == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer(freezerUUID).getName();


        //Bukkit.getLogger().info("[SF DEBUG] freezerName = null? " + (freezerName == null));

        String onlinePlaceholder = Bukkit.getPlayer(freezeeUUID) != null ? this.plugin.getConfig().getString("frozen-list-format.online-placeholder") : this.plugin.getConfig().getString("frozen-list-format.offline-placeholder");

        //Bukkit.getLogger().info("[SF DEBUG] onlinePlaceholder = null? " + (onlinePlaceholder == null));

        String timePlaceholder = this.plugin.getPlayerConfig().getConfig().isSet("players." + uuidStr + ".unfreeze-date") ? TimeUtil.formatTime((this.plugin.getPlayerConfig().getConfig().getLong("players." + uuidStr + ".unfreeze-date") - System.currentTimeMillis()) / 1000L) : "";

        //Bukkit.getLogger().info("[SF DEBUG] timePlaceholder = null? " + (timePlaceholder == null));

        if (!timePlaceholder.equals("")) {
            //Bukkit.getLogger().info("[SF DEBUG] temp freeze confirmed");
            if ((this.plugin.getPlayerConfig().getConfig().getLong("players." + uuidStr + ".unfreeze-date") - System.currentTimeMillis()) / 1000L <= 0) {
                //Bukkit.getLogger().info("[SF DEBUG] time already up, returning");
                return;
            }
            path = new StringBuilder("temp-frozen");
        }

        String locationName = this.locationManager.getLocationName(freezeLocation);
        if (locationName != null) {
            //Bukkit.getLogger().info("[SF DEBUG] locationName isn't null ? " + (locationName == null));
            path.append("-location");
        }

        String locationPlaceholder = this.locationManager.getLocationPlaceholder(locationName);

        //Bukkit.getLogger().info("[SF DEBUG] locationPlaceholder = null? " + (locationPlaceholder == null));

        String reason = this.plugin.getPlayerConfig().getConfig().getString("players." + freezeeUUID.toString() + ".reason", "");

        //Bukkit.getLogger().info("[SF DEBUG] reason = null? " + (reason == null));

        //Bukkit.getLogger().info("[SF DEBUG] String at path is null ? " + (this.plugin.getConfig().getString("frozen-list-format.formats." + path) == null));

        this.playerInfo.put(freezeeUUID, this.plugin.placeholders(this.plugin.getConfig().getString("frozen-list-format.formats." + path).replace("{ONLINE}", onlinePlaceholder).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{FREEZER}", freezerName).replace("{REASON}", reason)));
    }

    public void removePlayer(UUID uuid) {
        this.playerInfo.remove(uuid);
    }

    public void refreshStrings(HashSet<String> strings) {
        for (UUID freezeeUUID : this.playerInfo.keySet()) {

            String uuidStr = freezeeUUID.toString();
            StringBuilder path = new StringBuilder("frozen");

            String timePlaceholder = this.plugin.getPlayerConfig().getConfig().isSet("players." + uuidStr + ".unfreeze-date") ? TimeUtil.formatTime((this.plugin.getPlayerConfig().getConfig().getLong("players." + uuidStr + ".unfreeze-date") - System.currentTimeMillis()) / 1000L) : "";

            if (!timePlaceholder.equals("")) {
                if ((this.plugin.getPlayerConfig().getConfig().getLong("players." + uuidStr + ".unfreeze-date") - System.currentTimeMillis()) / 1000L <= 0) {
                    return;
                }
                path = new StringBuilder("temp-frozen");
            }

            Location freezeLocation = SFLocation.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freeze-location"));

            String locationName = this.locationManager.getLocationName(freezeLocation);
            if (locationName != null) {
                path.append("-location");
            }

            if (!strings.contains(path.toString())) {
                continue;
            }

            UUID freezerUUID = this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freezer-uuid").equals("null") ? null : UUID.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freezer-uuid"));

            String freezeeName = Bukkit.getPlayer(freezeeUUID) == null ? Bukkit.getOfflinePlayer(freezeeUUID).getName() : Bukkit.getPlayer(freezeeUUID).getName();

            String freezerName = freezerUUID == null ? "CONSOLE" : Bukkit.getPlayer(freezerUUID) == null ? Bukkit.getOfflinePlayer(freezerUUID).getName() : Bukkit.getPlayer(freezerUUID).getName();

            String onlinePlaceholder = Bukkit.getPlayer(freezeeUUID) != null ? this.plugin.getConfig().getString("frozen-list-format.online-placeholder") : this.plugin.getConfig().getString("frozen-list-format.offline-placeholder");

            String locationPlaceholder = this.locationManager.getLocationPlaceholder(locationName);

            String reason = this.plugin.getPlayerConfig().getConfig().getString("players." + freezeeUUID.toString() + ".reason", "");

            this.playerInfo.put(freezeeUUID, this.plugin.placeholders(this.plugin.getConfig().getString
                    ("frozen-list-format" + ".formats." + path).replace("{ONLINE}", onlinePlaceholder).replace("{PLAYER}", freezeeName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{FREEZER}", freezerName).replace("{REASON}", reason)));
        }
    }

    public String getPage(int page) {
        StringBuilder sb = new StringBuilder();
        int firstIndex = (page - 1) * this.plugin.getConfig().getInt("frozen-list-format.players-per-page");
        int lastIndex = page * this.plugin.getConfig().getInt("frozen-list-format.players-per-page");
        int index = 0;
        for (String string : this.playerInfo.values()) {
            if (index >= firstIndex && index <= lastIndex) {
                sb.append(string + "\n");
            }
            index++;
        }
        return sb.substring(0, sb.length() - 1);
    }

    public boolean noPages() {
        if (this.playerInfo.size() == 0) {
            return true;
        }
        return false;
    }

    public int getMaxPageNum() {
        return (int) Math.ceil((double) this.playerInfo.size() / this.plugin.getConfig().getDouble("frozen-list-format.players-per-page"));
    }

}
