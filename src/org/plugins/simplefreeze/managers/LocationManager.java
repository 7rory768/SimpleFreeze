package org.plugins.simplefreeze.managers;

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
        String locPlaceholder = this.plugin.getConfig().getString("location");
        if (location == null) {
            return locPlaceholder;
        }

        World world = location.getWorld();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        double yaw = location.getYaw();
        double pitch = location.getPitch();

        for (String locationName : this.plugin.getConfig().getConfigurationSection("locations").getKeys(false)) {
            if (this.plugin.getConfig().getString("locations." + locationName + ".worldname").equals(world.getName()) && this.plugin.getConfig().getDouble("locations." +
                    locationName + ".x") == x && this.plugin.getConfig().getDouble("locations." + locationName + ".y") == y && this.plugin.getConfig().getDouble("locations." + locationName + ".z") == z) {
                locPlaceholder = this.plugin.getConfig().getString("locations." + locationName + ".placeholder", locationName);
                if (this.plugin.getConfig().isSet("locations." + locationName + ".yaw") && this.plugin.getConfig().isSet("locations." + locationName + ".pitch")) {
                    if (Float.valueOf(this.plugin.getConfig().getString("locations." + locationName + ".yaw")) == yaw && Float.valueOf(this.plugin.getConfig().getString("locations." + locationName + ".pitch")) == pitch) {
                        return locPlaceholder;
                    }
                } else if (this.plugin.getConfig().isSet("locations." + locationName + ".yaw") && !this.plugin.getConfig().isSet("locations." + locationName + ".pitch")) {
                    if (Float.valueOf(this.plugin.getConfig().getString("locations." + locationName + ".yaw")) == yaw) {
                        return locPlaceholder;
                    }
                } else if (!this.plugin.getConfig().isSet("locations." + locationName + ".yaw") && this.plugin.getConfig().isSet("locations." + locationName + ".pitch")) {
                    if (Float.valueOf(this.plugin.getConfig().getString("locations." + locationName + ".pitch")) == pitch) {
                        return locPlaceholder;
                    }
                }
            }
        }

        return locPlaceholder;
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
