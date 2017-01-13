package org.plugins.simplefreeze.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
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

        for (String locationName : this.plugin.getConfig().getConfigurationSection("locations").getKeys(false)) {
            World locationWorld = Bukkit.getWorld(this.plugin.getConfig().getString("locations." + locationName + ".worldname"));
            double locationX = this.plugin.getConfig().getDouble("locations." + locationName + ".x");
            double locationY = this.plugin.getConfig().getDouble("locations." + locationName + ".y");
            double locationZ = this.plugin.getConfig().getDouble("locations." + locationName + ".z");
            float locationYaw = this.plugin.getConfig().isSet("locations." + locationName + ".yaw") ? Float.valueOf(this.plugin.getConfig().getString("locations." + locationName + ".yaw")) : yaw;
            float locationPitch = this.plugin.getConfig().isSet("locations." + locationName + ".pitch") ? Float.valueOf(this.plugin.getConfig().getString("locations." + locationName + ".pitch")) : pitch;
            if (world.getName().equals(locationWorld.getName()) && x == locationX && y == locationY && z == locationZ && yaw == locationYaw && pitch == locationPitch) {
                return locationName;
            }
        }
        return null;
    }

    public String getLocationPlaceholder(String locationName) {
        return this.plugin.getConfig().getString("locations." + locationName + ".placeholder", this.plugin.getConfig().getString("location"));
    }

    public SFLocation getHighestAirLocation(SFLocation pLoc) {
        World world = pLoc.getWorld();
        int x = pLoc.getBlockX();
        int z = pLoc.getBlockZ();
        for (int y = 256; y > 0; y--) {
            Block block = world.getBlockAt(new SFLocation(world, x, y, z));
            if (block.getType() != Material.AIR) {
                if (world.getBlockAt(new SFLocation(world, pLoc.getX(), pLoc.getY() + 1, pLoc.getZ())) == null && world.getBlockAt(new SFLocation(world, pLoc.getX(), pLoc.getY() + 2, pLoc.getZ())) == null) {
                    return new SFLocation(pLoc.getWorld(), pLoc.getX(), y + 1, pLoc.getZ(), pLoc.getYaw(), pLoc.getPitch());
                } else if (world.getBlockAt(new SFLocation(world, pLoc.getX(), pLoc.getY() + 1, pLoc.getZ())).getType() == Material.AIR && world.getBlockAt(new SFLocation(world, pLoc.getX(), pLoc.getY() + 2, pLoc.getZ())).getType() == Material.AIR) {
                    return new SFLocation(pLoc.getWorld(), pLoc.getX(), y + 1, pLoc.getZ(), pLoc.getYaw(), pLoc.getPitch());
                }
            }
        }
        return pLoc;
    }
}
