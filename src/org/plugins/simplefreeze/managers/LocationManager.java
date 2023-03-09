package org.plugins.simplefreeze.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.objects.SFLocation;

/**
 * Created by Rory on 11/29/2016.
 */
public class LocationManager {

    private final SimpleFreezeMain plugin;

    public LocationManager(SimpleFreezeMain plugin) {
        this.plugin = plugin;
    }

    public String getLocationName(org.bukkit.Location location) {
        if (location == null) {
            return null;
        }

        World world = location.getWorld();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        float yaw = location.getYaw();
        float pitch = location.getPitch();

        for (String locationName : this.plugin.getLocationsConfig().getConfig().getConfigurationSection("locations").getKeys(false)) {
            World locationWorld = Bukkit.getWorld(this.plugin.getLocationsConfig().getConfig().getString("locations." + locationName + ".worldname"));
            if (locationWorld != null) {
                double locationX = this.plugin.getLocationsConfig().getConfig().getDouble("locations." + locationName + ".x");
                double locationY = this.plugin.getLocationsConfig().getConfig().getDouble("locations." + locationName + ".y");
                double locationZ = this.plugin.getLocationsConfig().getConfig().getDouble("locations." + locationName + ".z");
                if (world.getName().equals(locationWorld.getName()) && x == locationX && y == locationY && z == locationZ) {
                    return locationName;
                }
            }
        }
        return null;
    }

    public SFLocation getSFLocation(String locName) {
        Location loc = this.getLocation(locName);
        if (loc != null) {
            return new SFLocation(loc);
        } else {
            return null;
        }
    }

    public Location getLocation(String locName) {
        for (String locationKey : this.plugin.getLocationsConfig().getConfig().getConfigurationSection("locations").getKeys(false)) {
            if (locationKey.equalsIgnoreCase(locName)) {
                if (this.plugin.getLocationsConfig().getConfig().isSet("locations." + locationKey) && this.plugin.getLocationsConfig().getConfig().isSet("locations." + locationKey + ".worldname") && this.plugin.getLocationsConfig().getConfig().isSet("locations." + locationKey + ".x")
                        && this.plugin.getLocationsConfig().getConfig().isSet("locations." + locationKey + ".y") && this.plugin.getLocationsConfig().getConfig().isSet("locations." + locationKey + ".z")) {
                    World world = Bukkit.getWorld(this.plugin.getLocationsConfig().getConfig().getString("locations." + locationKey + ".worldname"));
                    double x = this.plugin.getLocationsConfig().getConfig().getDouble("locations." + locationKey + ".x");
                    double y = this.plugin.getLocationsConfig().getConfig().getDouble("locations." + locationKey + ".y");
                    double z = this.plugin.getLocationsConfig().getConfig().getDouble("locations." + locationKey + ".z");
                    float yaw = this.plugin.getLocationsConfig().getConfig().isSet("locations." + locationKey + ".yaw") ? Float.valueOf(this.plugin.getLocationsConfig().getConfig().getString("locations." + locationKey + ".yaw")) : (float) 0.0;
                    float pitch = this.plugin.getLocationsConfig().getConfig().isSet("locations." + locationKey + ".pitch") ? Float.valueOf(this.plugin.getLocationsConfig().getConfig().getString("locations." + locationKey + ".pitch")) : (float) 0.0;
                    return new Location(world, x, y, z, yaw, pitch);
                }
            }
        }
        return null;
    }

    public int getTotalDistance(Player p1, Player p2) {
        Location loc1 = p1.getLocation();
        Location loc2 = p2.getLocation();
        int distance = (int) Math.round(Math.sqrt(Math.pow(Math.abs(loc1.getX() - loc2.getX()), 2) + Math.pow(Math.abs(loc1.getZ() - loc2.getZ()), 2)));
        if (this.plugin.getConfig().getBoolean("include-y")) {
            distance = (int) Math.round(Math.sqrt(Math.pow(Math.abs(loc1.getY() - loc2.getY()), 2) + Math.pow(distance, 2)));
        }
        return distance;
    }

    public String getLocationPlaceholder(String locationName) {
        if (locationName == null) {
            return this.plugin.getConfig().getString("empty-location", "");
        }
        return this.plugin.getLocationsConfig().getConfig().getString("locations." + locationName.toLowerCase() + ".placeholder", this.plugin.getConfig().getString("empty-location"));
    }

    public Location getGroundLocation(Location pLoc) {
        World world = pLoc.getWorld();
        int x = pLoc.getBlockX();
        int z = pLoc.getBlockZ();
        for (int y = pLoc.getBlockY(); y >= 0; y--) {
            Block block = world.getBlockAt(new SFLocation(world, x, y, z));
            if (block.getType() != Material.AIR) {
                if (block.getType().isSolid()) {
                    return new SFLocation(world, pLoc.getX(), y + 1, pLoc.getZ(), pLoc.getYaw(), pLoc.getPitch());
                }
            }
        }
        return pLoc;
    }

/*    public List<String> getLocations() {
        List<String> locations = new ArrayList<>();
        for (String location : this.plugin.getLocationsConfig().getConfig().getConfigurationSection("locations").getKeys(false)) {
            locations.add(location);
        }
        return locations;
    }*/
}
