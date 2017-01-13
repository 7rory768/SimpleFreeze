package org.plugins.simplefreeze.objects;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class SFLocation extends org.bukkit.Location {

    public SFLocation(org.bukkit.Location loc) {
        super(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public SFLocation(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public SFLocation(World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        return this.getWorld().getName() + "|" + this.getX() + "|" + this.getY() + "|" + this.getZ() + "|" + this.getYaw() + "|" + this.getPitch();
    }

    public static SFLocation fromString(String arg) {
        if (arg == null) {
            return null;
        } else if (arg.equals("null")) {
            return null;
        }
        String[] info = arg.split("\\|");
        return new SFLocation(Bukkit.getWorld(info[0]), Double.parseDouble(info[1]), Double.parseDouble(info[2]), Double.parseDouble(info[3]), Float.parseFloat(info[4]), Float.parseFloat(info[5]));
    }

}
