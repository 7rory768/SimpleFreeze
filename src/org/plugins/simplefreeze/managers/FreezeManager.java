package org.plugins.simplefreeze.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.objects.FrozenPlayer;
import org.plugins.simplefreeze.objects.SFLocation;
import org.plugins.simplefreeze.objects.TempFrozenPlayer;
import org.plugins.simplefreeze.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FreezeManager {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;
    private final SQLManager sqlManager;

    private ItemStack helmetItem;

    public FreezeManager(SimpleFreezeMain plugin, PlayerManager playerManager, SQLManager sqlManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.sqlManager = sqlManager;
        this.setupHelmetItem();
    }

    private void setupHelmetItem() {
        ItemStack helmetItem = new ItemStack(Material.getMaterial(this.plugin.getConfig().getString("head-item.material")), 1, (short) this.plugin.getConfig().getInt("head-item.data"));
        ItemMeta helmetMeta = helmetItem.getItemMeta();
        if (this.plugin.getConfig().isSet("head-item.name")) {
            // More placeholders should be added here (location, freezer, time)
            // Also allow enchants and itemflags
            helmetMeta.setDisplayName(this.plugin.placeholders(this.plugin.getConfig().getString("head-item.name")));
        }
        if (this.plugin.getConfig().isSet("head-item.lore")) {
            List<String> lore = new ArrayList<String>();
            for (String loreLine : this.plugin.getConfig().getStringList("head-item.lore")) {
                lore.add(this.plugin.placeholders(loreLine));
            }
            helmetMeta.setLore(lore);
        }
        helmetItem.setItemMeta(helmetMeta);
        this.helmetItem = helmetItem;
    }

    public void updateHelmetItem(ItemStack newHelmetItem) {
        this.helmetItem = newHelmetItem;
    }

    public boolean similarToHelmetItem(ItemStack newHelmetItem) {
        return this.helmetItem.isSimilar(newHelmetItem);
    }

    public void replaceOldHelmets() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (this.playerManager.isFrozen(p)) {
                p.getInventory().setHelmet(this.helmetItem);
            }
        }
    }

    private SFLocation getHighestAirLocation(SFLocation pLoc) {
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

    public void unfreeze(UUID uuid) {
        if (this.playerManager.isFrozen(uuid)) {
            FrozenPlayer frozenPlayer = this.playerManager.getFrozenPlayer(uuid);
            if (frozenPlayer.isOnline()) {
                Bukkit.getPlayer(uuid).getInventory().setHelmet(frozenPlayer.getHelmet());
                Bukkit.getPlayer(uuid).teleport(frozenPlayer.getOriginalLoc());
            }
            this.plugin.getPlayerConfig().getConfig().set("players." + uuid.toString(), null);
            this.plugin.getPlayerConfig().saveConfig();
            this.plugin.getPlayerConfig().reloadConfig();
            this.playerManager.removeFrozenPlayer(uuid);
        }
    }

    public void freeze(UUID freezeeUUID, String freezeeName, String freezerName, String location) {
        boolean online = false;
        ItemStack helmet = null;
        SFLocation originalLoc = null;
        SFLocation freezeLoc = null;
        Player onlineFreezee = Bukkit.getPlayer(freezeeUUID);
        String playerPlaceholder = freezeeName;
        String freezerPlaceholder = freezerName;
        String locationPlaceholder = location == null ? "" : this.plugin.getConfig().getString("locations." + location + ".placeholder", location);
        String timePlaceholder = "";
        String serversPlaceholder = "";

        if (location != null) {
            if (this.plugin.getConfig().isSet("locations." + location) && this.plugin.getConfig().isSet("locations." + location + ".worldname") && this.plugin.getConfig().isSet("locations." + location + ".x")
                    && this.plugin.getConfig().isSet("locations." + location + ".y") && this.plugin.getConfig().isSet("locations." + location + ".z")) {
                World world = Bukkit.getWorld(this.plugin.getConfig().getString("locations." + location + ".worldname"));
                double x = this.plugin.getConfig().getDouble("locations." + location + ".x");
                double y = this.plugin.getConfig().getDouble("locations." + location + ".y");
                double z = this.plugin.getConfig().getDouble("locations." + location + ".z");
                float yaw = this.plugin.getConfig().isSet("locations." + location + ".yaw") ? Float.valueOf(this.plugin.getConfig().getString("locations." + location + ".yaw")) : (float) 0.0;
                if (!this.plugin.getConfig().isSet("locations." + location + ".yaw") && onlineFreezee != null) {
                    yaw = onlineFreezee.getLocation().getYaw();
                }
                float pitch = this.plugin.getConfig().isSet("locations." + location + ".pitch") ? Float.valueOf(this.plugin.getConfig().getString("locations." + location + ".pitch")) : (float) 0.0;
                if (!this.plugin.getConfig().isSet("locations." + location + ".pitch") && onlineFreezee != null) {
                    pitch = onlineFreezee.getLocation().getPitch();
                }
                freezeLoc = new SFLocation(world, x, y, z, yaw, pitch);
            }
        }

        if (onlineFreezee != null) {
            online = true;
            if (onlineFreezee.getInventory().getHelmet() != null) {
                helmet = onlineFreezee.getInventory().getHelmet();
            }
            ItemStack helmetItem = this.helmetItem.clone();
            ItemMeta helmetMeta = helmetItem.getItemMeta();
            if (helmetMeta.hasDisplayName()) {
                // More placeholders should be added here (location, freezer, time)
                // Also allow enchants and itemflags
                helmetMeta.setDisplayName(this.plugin.placeholders(helmetMeta.getDisplayName().replace("{FREEZER}", freezerPlaceholder).replace("{PLAYER}", playerPlaceholder).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
            }
            if (helmetMeta.hasLore()) {
                List<String> lore = new ArrayList<String>();
                for (String loreLine : helmetMeta.getLore()) {
                    lore.add(this.plugin.placeholders(loreLine.replace("{FREEZER}", freezerPlaceholder).replace("{PLAYER}", playerPlaceholder).replace("{TIME}", timePlaceholder)
                            .replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
                }
                helmetMeta.setLore(lore);
            }
            helmetItem.setItemMeta(helmetMeta);
            onlineFreezee.getInventory().setHelmet(helmetItem);
            originalLoc = new SFLocation(onlineFreezee.getLocation());
            if (freezeLoc == null && this.plugin.getConfig().getBoolean("teleport-up")) {
                freezeLoc = this.getHighestAirLocation(originalLoc);
            } else if (freezeLoc == null) {
                freezeLoc = new SFLocation(originalLoc.clone());
                if (this.plugin.getConfig().getBoolean("enable-fly")) {
                    onlineFreezee.setFlying(true);
                }
            }
            if (!freezeLoc.equals(originalLoc)) {
                onlineFreezee.teleport(freezeLoc);
            }
        }

        long freezeDate = System.currentTimeMillis();
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freeze-date", freezeDate);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freezee-name", freezeeName);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freezer-name", freezerName);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".orginal-location", originalLoc == null ? "null" : originalLoc.toString());
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freeze-location", freezeLoc == null ? "null" : freezeLoc.toString());
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".helmet", helmet == null ? "null" : helmet);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".mysql", false);
        this.plugin.getPlayerConfig().saveConfig();
        this.plugin.getPlayerConfig().reloadConfig();
        FrozenPlayer frozenPlayer = new FrozenPlayer(freezeDate, freezeeUUID, freezeeName, freezerName, originalLoc, freezeLoc, false, online, helmet);
        this.playerManager.addFrozenPlayer(freezeeUUID, frozenPlayer);
    }

    public void tempFreeze(final UUID freezeeUUID, final String freezeeName, String freezerName, String location, long time) {
        boolean online = false;
        ItemStack helmet = null;
        SFLocation originalLoc = null;
        SFLocation freezeLoc = null;
        final Player onlineFreezee = Bukkit.getPlayer(freezeeUUID);
        String playerPlaceholder = freezeeName;
        String freezerPlaceholder = freezerName;
        String locationPlaceholder = location == null ? "" : this.plugin.getConfig().getString("locations." + location + ".placeholder", location);
        String timePlaceholder = TimeUtil.formatTime(time);
        String serversPlaceholder = "";

        if (location != null) {
            if (this.plugin.getConfig().isSet("locations." + location) && this.plugin.getConfig().isSet("locations." + location + ".worldname") && this.plugin.getConfig().isSet("locations." + location + ".x")
                    && this.plugin.getConfig().isSet("locations." + location + ".y") && this.plugin.getConfig().isSet("locations." + location + ".z")) {
                World world = Bukkit.getWorld(this.plugin.getConfig().getString("locations." + location + ".worldname"));
                double x = this.plugin.getConfig().getDouble("locations." + location + ".x");
                double y = this.plugin.getConfig().getDouble("locations." + location + ".y");
                double z = this.plugin.getConfig().getDouble("locations." + location + ".z");
                float yaw = this.plugin.getConfig().isSet("locations." + location + ".yaw") ? Float.valueOf(this.plugin.getConfig().getString("locations." + location + ".yaw")) : (float) 0.0;
                if (!this.plugin.getConfig().isSet("locations." + location + ".yaw") && onlineFreezee != null) {
                    yaw = onlineFreezee.getLocation().getYaw();
                }
                float pitch = this.plugin.getConfig().isSet("locations." + location + ".pitch") ? Float.valueOf(this.plugin.getConfig().getString("locations." + location + ".pitch")) : (float) 0.0;
                if (!this.plugin.getConfig().isSet("locations." + location + ".pitch") && onlineFreezee != null) {
                    pitch = onlineFreezee.getLocation().getPitch();
                }
                freezeLoc = new SFLocation(world, x, y, z, yaw, pitch);
            }
        }

        if (onlineFreezee != null) {
            online = true;
            if (onlineFreezee.getInventory().getHelmet() != null) {
                helmet = onlineFreezee.getInventory().getHelmet();
            }
            ItemStack helmetItem = this.helmetItem.clone();
            ItemMeta helmetMeta = helmetItem.getItemMeta();
            if (helmetMeta.hasDisplayName()) {
                // More placeholders should be added here (location, freezer, time)
                // Also allow enchants and itemflags
                helmetMeta.setDisplayName(this.plugin.placeholders(helmetMeta.getDisplayName().replace("{FREEZER}", freezerPlaceholder).replace("{PLAYER}", playerPlaceholder).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
            }
            if (helmetMeta.hasLore()) {
                List<String> lore = new ArrayList<String>();
                for (String loreLine : helmetMeta.getLore()) {
                    lore.add(this.plugin.placeholders(loreLine.replace("{FREEZER}", freezerPlaceholder).replace("{PLAYER}", playerPlaceholder).replace("{TIME}", timePlaceholder)
                            .replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
                }
                helmetMeta.setLore(lore);
            }
            helmetItem.setItemMeta(helmetMeta);
            onlineFreezee.getInventory().setHelmet(helmetItem);
            originalLoc = new SFLocation(onlineFreezee.getLocation());
            if (freezeLoc == null && this.plugin.getConfig().getBoolean("teleport-up")) {
                freezeLoc = this.getHighestAirLocation(originalLoc);
            } else if (freezeLoc == null) {
                freezeLoc = new SFLocation(originalLoc.clone());
                if (this.plugin.getConfig().getBoolean("enable-fly")) {
                    onlineFreezee.setFlying(true);
                }
            }
            if (!freezeLoc.equals(originalLoc)) {
                onlineFreezee.teleport(freezeLoc);
            }
        }

        long freezeDate = System.currentTimeMillis();
        long unfreezeDate = freezeDate + (time * 1000L);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freeze-date", freezeDate);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".unfreeze-date", unfreezeDate);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freezee-name", freezeeName);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freezer-name", freezerName);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".orginal-location", originalLoc == null ? "null" : originalLoc.toString());
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".freeze-location", freezeLoc == null ? "null" : freezeLoc.toString());
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".helmet", helmet == null ? "null" : helmet);
        this.plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString() + ".mysql", false);
        this.plugin.getPlayerConfig().saveConfig();
        this.plugin.getPlayerConfig().reloadConfig();
        final TempFrozenPlayer tempFrozenPlayer = new TempFrozenPlayer(freezeDate, unfreezeDate, freezeeUUID, freezeeName, freezerName, originalLoc, freezeLoc, false, online, helmet);
        BukkitTask task;
        if (!tempFrozenPlayer.isSqlFreeze()) {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (tempFrozenPlayer.isOnline()) {
                        for (String msg : plugin.getConfig().getStringList("unfreeze-message")) {
                            onlineFreezee.sendMessage(plugin.placeholders(msg).replace("{PLAYER}", freezeeName));
                        }
                        onlineFreezee.getInventory().setHelmet(tempFrozenPlayer.getHelmet());
                        onlineFreezee.teleport(tempFrozenPlayer.getOriginalLoc());
                    }
                    plugin.getPlayerConfig().getConfig().set("players." + freezeeUUID.toString(), null);
                    plugin.getPlayerConfig().saveConfig();
                    plugin.getPlayerConfig().reloadConfig();
                    playerManager.removeFrozenPlayer(freezeeUUID);

                }
            }.runTaskLater(this.plugin, time * 20L);
        } else {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    //SQL TABLE STUFF
                }
            }.runTaskLater(this.plugin, time * 20L);
        }
        tempFrozenPlayer.setTask(task);
        FrozenPlayer frozenPlayer = (FrozenPlayer) tempFrozenPlayer;
        this.playerManager.addFrozenPlayer(freezeeUUID, frozenPlayer);
    }

    public void notifyOfFreeze(CommandSender sender, UUID uuid, String location) {
        FrozenPlayer frozenPlayer = this.playerManager.getFrozenPlayer(uuid);
        Player onlineFreezee = Bukkit.getPlayer(uuid);
        String playerPlaceholder = frozenPlayer.getFreezeeName();
        String freezerPlaceholder = frozenPlayer.getFreezerName();
        String timePlaceholder = "";
        String serversPlaceholder = "";
        String locationPlaceholder = location == null ? "" : this.plugin.getConfig().getString("locations." + location + ".placeholder", location);
        String playerMessage;
        String notifyMessage;
        if (frozenPlayer instanceof TempFrozenPlayer) {
            timePlaceholder = TimeUtil.formatTime((((TempFrozenPlayer) frozenPlayer).getUnfreezeDate() - System.currentTimeMillis()) / 1000L);
            playerMessage = "temp-freeze-message";
            if (location != null) {
                playerMessage = "temp-freeze-location-message";
            }
            notifyMessage = "temp-frozen-notify-message";
            if (frozenPlayer.isSqlFreeze()) {
                notifyMessage = "sql-temp-frozen-notify-message";
            }
        } else {
            playerMessage = "freeze-message";
            if (location != null) {
                playerMessage = "freeze-location-message";
            }
            notifyMessage = "frozen-notify-message";
            if (frozenPlayer.isSqlFreeze()) {
                notifyMessage = "sql-frozen-notify-message";
            }
        }

        if (onlineFreezee != null) {
            for (String msg : this.plugin.getConfig().getStringList(playerMessage)) {
                if (!msg.equals("")) {
                    onlineFreezee.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerPlaceholder).replace("{PLAYER}", playerPlaceholder).replace("{TIME}", timePlaceholder)
                            .replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
                }
            }
        }
        for (String msg : this.plugin.getConfig().getStringList(notifyMessage)) {
            if (!msg.equals("")) {
                sender.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerPlaceholder).replace("{PLAYER}", playerPlaceholder).replace("{TIME}", timePlaceholder)
                        .replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
            }
        }
        Player senderP = sender instanceof Player ? (Player) sender : null;
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.hasPermission("sf.notify.freeze") && p != senderP) {
                for (String msg : this.plugin.getConfig().getStringList(notifyMessage)) {
                    if (!msg.equals("")) {
                        sender.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerPlaceholder).replace("{PLAYER}", playerPlaceholder).replace("{TIME}", timePlaceholder)
                                .replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder)));
                    }
                }
            }
        }
    }

    public void notifyOfUnfreeze(CommandSender sender, UUID uuid) {
        FrozenPlayer frozenPlayer = this.playerManager.getFrozenPlayer(uuid);
        Player onlineFreezee = Bukkit.getPlayer(uuid);
        String playerPlaceholder = frozenPlayer.getFreezeeName();
        String freezerPlaceholder = frozenPlayer.getFreezerName();

        if (onlineFreezee != null) {
            for (String msg : this.plugin.getConfig().getStringList("unfreeze-message")) {
                if (!msg.equals("")) {
                    onlineFreezee.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerPlaceholder).replace("{PLAYER}", playerPlaceholder)));
                }
            }
        }

        for (String msg : this.plugin.getConfig().getStringList("unfrozen-notify-message")) {
            if (!msg.equals("")) {
                sender.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerPlaceholder).replace("{PLAYER}", playerPlaceholder)));
            }
        }
        Player senderP = sender instanceof Player ? (Player) sender : null;
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.hasPermission("sf.notify.freeze") && p != senderP) {
                for (String msg : this.plugin.getConfig().getStringList("unfrozen-notify-message")) {
                    if (!msg.equals("")) {
                        sender.sendMessage(this.plugin.placeholders(msg.replace("{FREEZER}", freezerPlaceholder).replace("{PLAYER}", playerPlaceholder)));
                    }
                }
            }
        }
    }

}
