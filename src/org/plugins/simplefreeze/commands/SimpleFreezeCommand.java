package org.plugins.simplefreeze.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.cache.FrozenPages;
import org.plugins.simplefreeze.managers.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SimpleFreezeCommand implements CommandExecutor {

    private final SimpleFreezeMain plugin;
    private final HelmetManager helmetManager;
    private final FrozenPages frozenPages;
    private final ParticleManager particleManager;
    private final SoundManager soundManager;
    private final MessageManager messageManager;
    private final MovementManager movementManager;

    public SimpleFreezeCommand(SimpleFreezeMain plugin, HelmetManager helmetManager, FrozenPages frozenPages, ParticleManager particleManager, SoundManager soundManager, MessageManager messageManager, MovementManager movementManager) {
        this.plugin = plugin;
        this.helmetManager = helmetManager;
        this.frozenPages = frozenPages;
        this.particleManager = particleManager;
        this.soundManager = soundManager;
        this.messageManager = messageManager;
        this.movementManager = movementManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("simplefreeze")) {

            if (args.length > 0) {

                if (args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("sf.reload")) {
                        for (String msg : this.plugin.getConfig().getStringList("no-permission-message")) {
                            sender.sendMessage(this.plugin.placeholders(msg));
                        }
                        return false;
                    }

                    // STORE OLD SOUND VALUES
                    float freezeVolume = (float) this.plugin.getConfig().getDouble("freeze-sound-volume");
                    float freezePitch = (float) this.plugin.getConfig().getDouble("freeze-sound-pitch");

                    float unfreezeVolume = (float) this.plugin.getConfig().getDouble("unfreeze-sound-volume");
                    float unfreezePitch = (float) this.plugin.getConfig().getDouble("unfreeze-sound-pitch");

                    // STORE OLD /FROZEN FORMATS
                    String frozenStr = this.plugin.getConfig().getString("frozen-list-format.formats.frozen");
                    String frozenLocStr = this.plugin.getConfig().getString("frozen-list-format.formats.frozen-location");
                    String tempFrozenStr = this.plugin.getConfig().getString("frozen-list-format.formats.temp-frozen");
                    String tempFrozenLocStr = this.plugin.getConfig().getString("frozen-list-format.formats.temp-frozen-location");
                    String onlinePlaceholder = this.plugin.getConfig().getString("frozen-list-format.online-placeholder");
                    String offlinePlaceholder = this.plugin.getConfig().getString("frozen-list-format.offline-placeholder");

                    // RELOAD CONFIG
                    this.plugin.reloadConfig();
                    this.plugin.updateFinalPrefixFormatting();

                    // CHECK IF SOUND VALUES CHANGED
                    String newFreezeSound = this.plugin.getConfig().getString("freeze-sound.sound");
                    float newFreezeVolume = (float) this.plugin.getConfig().getDouble("freeze-sound-volume");
                    float newFreezePitch = (float) this.plugin.getConfig().getDouble("freeze-sound-pitch");

                    String newUnfreezeSound = this.plugin.getConfig().getString("unfreeze-sound.sound");
                    float newUnfreezeVolume = (float) this.plugin.getConfig().getDouble("unfreeze-sound-volume");
                    float newUnfreezePitch = (float) this.plugin.getConfig().getDouble("unfreeze-sound-pitch");

                    if (!this.soundManager.setFreezeSound(newFreezeSound)) {
                        sender.sendMessage(this.plugin.placeholders("&c&lERROR: &7Invalid freeze sound: &c" + this.plugin.getConfig().getString("freeze-sound.sound")));
                    }

                    if (!this.soundManager.setUnfreezeSound(newUnfreezeSound)) {
                        sender.sendMessage(this.plugin.placeholders("&c&lERROR: &7Invalid unfreeze sound: &c" + this.plugin.getConfig().getString("unfreeze-sound.sound")));
                    }

                    if (freezeVolume != newFreezeVolume) {
                        this.soundManager.setFreezeVolume(newFreezeVolume);
                    }

                    if (unfreezeVolume != newUnfreezeVolume) {
                        this.soundManager.setUnfreezeVolume(newUnfreezeVolume);
                    }

                    if (freezePitch != newFreezePitch) {
                        this.soundManager.setFreezePitch(newFreezePitch);
                    }

                    if (unfreezePitch != newUnfreezePitch) {
                        this.soundManager.setUnfreezePitch(newUnfreezePitch);
                    }

                    // CHANGE MESSAGE-INTERVALS
                    this.messageManager.setFreezeInterval(this.plugin.getConfig().getInt("message-intervals.freeze"));
                    this.messageManager.setFreezeLocInterval(this.plugin.getConfig().getInt("message-intervals.freeze-location"));
                    this.messageManager.setTempFreezeInterval(this.plugin.getConfig().getInt("message-intervals.temp-freeze"));
                    this.messageManager.setTempFreezeLocInterval(this.plugin.getConfig().getInt("message-intervals.temp-freeze-location"));
                    this.messageManager.setFreezeAllInterval(this.plugin.getConfig().getInt("message-intervals.freeze-all"));
                    this.messageManager.setFreezeAllLocInterval(this.plugin.getConfig().getInt("message-intervals.freeze-all-location"));

                    // CHANGE HEAD-MOVEMENT BOOLEAN
                    this.movementManager.setHeadMovementBoolean(this.plugin.getConfig().getBoolean("head-movement"));

                    // CHECK IF /FROZEN FORMATS CHANGED
                    HashSet<String> strings = new HashSet<String>();

                    if (!frozenStr.equals(this.plugin.getConfig().getString("frozen-list-format.formats.frozen"))) {
                        strings.add("frozen");
                    }
                    if (!frozenLocStr.equals(this.plugin.getConfig().getString("frozen-list-format.formats.frozen-location"))) {
                        strings.add("frozen-location");
                    }
                    if (!tempFrozenStr.equals(this.plugin.getConfig().getString("frozen-list-format.formats.temp-frozen"))) {
                        strings.add("temp-frozen");
                    }
                    if (!tempFrozenLocStr.equals(this.plugin.getConfig().getString("frozen-list-format.formats.temp-frozen-location"))) {
                        strings.add("temp-frozen-location");
                    }

                    if (!onlinePlaceholder.equals(this.plugin.getConfig().getString("frozen-list-format.online-placeholder"))) {
                        if (this.plugin.getConfig().getString("frozen-list-format.formats.frozen").contains("{ONLINE}")) {
                            strings.add("frozen");
                        }
                        if (this.plugin.getConfig().getString("frozen-list-format.formats.frozen-location").contains("{ONLINE}")) {
                            strings.add("frozen-location");
                        }
                        if (this.plugin.getConfig().getString("frozen-list-format.formats.temp-frozen").contains("{ONLINE}")) {
                            strings.add("temp-frozen");
                        }
                        if (this.plugin.getConfig().getString("frozen-list-format.formats.temp-frozen-location").contains("{ONLINE}")) {
                            strings.add("temp-frozen-location");
                        }
                    }

                    if (!offlinePlaceholder.equals(this.plugin.getConfig().getString("frozen-list-format.offline-placeholder"))) {
                        if (this.plugin.getConfig().getString("frozen-list-format.formats.frozen").contains("{OFFLINE}")) {
                            strings.add("frozen");
                        }
                        if (this.plugin.getConfig().getString("frozen-list-format.formats.frozen-location").contains("{OFFLINE}")) {
                            strings.add("frozen-location");
                        }
                        if (this.plugin.getConfig().getString("frozen-list-format.formats.temp-frozen").contains("{OFFLINE}")) {
                            strings.add("temp-frozen");
                        }
                        if (this.plugin.getConfig().getString("frozen-list-format.formats.temp-frozen-location").contains("{OFFLINE}")) {
                            strings.add("temp-frozen-location");
                        }
                    }

                    if (!strings.isEmpty()) {
                        this.frozenPages.refreshStrings(strings);
                    }

                    // REPLACE HELMETS IF THEY CHANGED,
                    ItemStack oldFrozenHelmet = this.helmetManager.getFrozenHelmet();
                    ItemStack newHelmet = null;

                    if (this.plugin.getConfig().isSet("head-item.frozen")) {
                        short data = this.plugin.getConfig().isSet("head-item.frozen.data") ? (short) this.plugin.getConfig().getInt("head-item.frozen.data") : 0;
                        newHelmet = new ItemStack(Material.getMaterial(this.plugin.getConfig().getString("head-item.frozen.material")), 1, data);
                        ItemMeta helmetMeta = newHelmet.getItemMeta();
                        if (this.plugin.getConfig().isSet("head-item.frozen.name")) {
                            // More placeholders should be added here (location, freezer, time)
                            // Also allow enchants and itemflags
                            helmetMeta.setDisplayName(this.plugin.placeholders(this.plugin.getConfig().getString("head-item.frozen.name")));
                        }
                        if (this.plugin.getConfig().isSet("head-item.frozen.lore")) {
                            List<String> lore = new ArrayList<String>();
                            for (String loreLine : this.plugin.getConfig().getStringList("head-item.frozen.lore")) {
                                lore.add(this.plugin.placeholders(loreLine));
                            }
                            helmetMeta.setLore(lore);
                        }
                        newHelmet.setItemMeta(helmetMeta);
                    }
                    if (!this.helmetManager.similarToHelmetItem(oldFrozenHelmet, newHelmet)) {
                        this.helmetManager.updateFrozenHelmet(newHelmet);
                        this.helmetManager.replaceOldHelmets();
                    }

                    ItemStack oldFrozenLocationHelmet = this.helmetManager.getFrozenLocationHelmet();
                    newHelmet = null;

                    if (this.plugin.getConfig().isSet("head-item.frozen-location")) {
                        short data = this.plugin.getConfig().isSet("head-item.frozen-location.data") ? (short) this.plugin.getConfig().getInt("head-item.frozen-location.data") : 0;
                        newHelmet = new ItemStack(Material.getMaterial(this.plugin.getConfig().getString("head-item.frozen-location.material")), 1, data);
                        ItemMeta helmetMeta = newHelmet.getItemMeta();
                        if (this.plugin.getConfig().isSet("head-item.frozen-location.name")) {
                            // More placeholders should be added here (location, freezer, time)
                            // Also allow enchants and itemflags
                            helmetMeta.setDisplayName(this.plugin.placeholders(this.plugin.getConfig().getString("head-item.frozen-location.name")));
                        }
                        if (this.plugin.getConfig().isSet("head-item.frozen-location.lore")) {
                            List<String> lore = new ArrayList<String>();
                            for (String loreLine : this.plugin.getConfig().getStringList("head-item.frozen-location.lore")) {
                                lore.add(this.plugin.placeholders(loreLine));
                            }
                            helmetMeta.setLore(lore);
                        }
                        newHelmet.setItemMeta(helmetMeta);
                    }
                    if (!this.helmetManager.similarToHelmetItem(oldFrozenLocationHelmet, newHelmet)) {
                        this.helmetManager.updateFrozenLocationHelmet(newHelmet);
                        this.helmetManager.replaceOldHelmets();
                    }

                    ItemStack oldTempFrozenHelmet = this.helmetManager.getTempFrozenHelmet();
                    newHelmet = null;

                    if (this.plugin.getConfig().isSet("head-item.temp-frozen")) {
                        short data = this.plugin.getConfig().isSet("head-item.temp-frozen.data") ? (short) this.plugin.getConfig().getInt("head-item.temp-frozen.data") : 0;
                        newHelmet = new ItemStack(Material.getMaterial(this.plugin.getConfig().getString("head-item.temp-frozen.material")), 1, data);
                        ItemMeta helmetMeta = newHelmet.getItemMeta();
                        if (this.plugin.getConfig().isSet("head-item.temp-frozen.name")) {
                            // More placeholders should be added here (location, freezer, time)
                            // Also allow enchants and itemflags
                            helmetMeta.setDisplayName(this.plugin.placeholders(this.plugin.getConfig().getString("head-item.temp-frozen.name")));
                        }
                        if (this.plugin.getConfig().isSet("head-item.temp-frozen.lore")) {
                            List<String> lore = new ArrayList<String>();
                            for (String loreLine : this.plugin.getConfig().getStringList("head-item.temp-frozen.lore")) {
                                lore.add(this.plugin.placeholders(loreLine));
                            }
                            helmetMeta.setLore(lore);
                        }
                        newHelmet.setItemMeta(helmetMeta);
                    }
                    if (!this.helmetManager.similarToHelmetItem(oldTempFrozenHelmet, newHelmet)) {
                        this.helmetManager.updateTempFrozenHelmet(newHelmet);
                        this.helmetManager.replaceOldHelmets();
                    }

                    ItemStack oldTempFrozenLocationHelmet = this.helmetManager.getTempFrozenLocationHelmet();
                    newHelmet = null;

                    if (this.plugin.getConfig().isSet("head-item.temp-frozen-location")) {
                        short data = this.plugin.getConfig().isSet("head-item.temp-frozen-location.data") ? (short) this.plugin.getConfig().getInt("head-item.temp-frozen-location.data") : 0;
                        newHelmet = new ItemStack(Material.getMaterial(this.plugin.getConfig().getString("head-item.temp-frozen-location.material")), 1, data);
                        ItemMeta helmetMeta = newHelmet.getItemMeta();
                        if (this.plugin.getConfig().isSet("head-item.temp-frozen-location.name")) {
                            // More placeholders should be added here (location, freezer, time)
                            // Also allow enchants and itemflags
                            helmetMeta.setDisplayName(this.plugin.placeholders(this.plugin.getConfig().getString("head-item.temp-frozen-location.name")));
                        }
                        if (this.plugin.getConfig().isSet("head-item.temp-frozen-location.lore")) {
                            List<String> lore = new ArrayList<String>();
                            for (String loreLine : this.plugin.getConfig().getStringList("head-item.temp-frozen-location.lore")) {
                                lore.add(this.plugin.placeholders(loreLine));
                            }
                            helmetMeta.setLore(lore);
                        }
                        newHelmet.setItemMeta(helmetMeta);
                    }
                    if (!this.helmetManager.similarToHelmetItem(oldTempFrozenLocationHelmet, newHelmet)) {
                        this.helmetManager.updateTempFrozenLocationHelmet(newHelmet);
                        this.helmetManager.replaceOldHelmets();
                    }

                    ItemStack oldFreezeAllHelmet = this.helmetManager.getFreezeAllHelmet();
                    newHelmet = null;

                    if (this.plugin.getConfig().isSet("head-item.freeze-all")) {
                        short data = this.plugin.getConfig().isSet("head-item.freeze-all.data") ? (short) this.plugin.getConfig().getInt("head-item.freeze-all.data") : 0;
                        newHelmet = new ItemStack(Material.getMaterial(this.plugin.getConfig().getString("head-item.freeze-all.material")), 1, data);
                        ItemMeta helmetMeta = newHelmet.getItemMeta();
                        if (this.plugin.getConfig().isSet("head-item.freeze-all.name")) {
                            // More placeholders should be added here (location, freezer, time)
                            // Also allow enchants and itemflags
                            helmetMeta.setDisplayName(this.plugin.placeholders(this.plugin.getConfig().getString("head-item.freeze-all.name")));
                        }
                        if (this.plugin.getConfig().isSet("head-item.freeze-all.lore")) {
                            List<String> lore = new ArrayList<String>();
                            for (String loreLine : this.plugin.getConfig().getStringList("head-item.freeze-all.lore")) {
                                lore.add(this.plugin.placeholders(loreLine));
                            }
                            helmetMeta.setLore(lore);
                        }
                        newHelmet.setItemMeta(helmetMeta);
                    }
                    if (!this.helmetManager.similarToHelmetItem(oldFreezeAllHelmet, newHelmet)) {
                        this.helmetManager.updateFreezeAllHelmet(newHelmet);
                        this.helmetManager.replaceOldHelmets();
                    }

                    ItemStack oldFreezeAllLocationHelmet = this.helmetManager.getFreezeAllLocationHelmet();
                    newHelmet = null;

                    if (this.plugin.getConfig().isSet("head-item.freeze-all-location")) {
                        short data = this.plugin.getConfig().isSet("head-item.freeze-all-location.data") ? (short) this.plugin.getConfig().getInt("head-item.freeze-all-location.data") : 0;
                        newHelmet = new ItemStack(Material.getMaterial(this.plugin.getConfig().getString("head-item.freeze-all-location.material")), 1, data);
                        ItemMeta helmetMeta = newHelmet.getItemMeta();
                        if (this.plugin.getConfig().isSet("head-item.freeze-all-location.name")) {
                            // More placeholders should be added here (location, freezer, time)
                            // Also allow enchants and itemflags
                            helmetMeta.setDisplayName(this.plugin.placeholders(this.plugin.getConfig().getString("head-item.freeze-all-location.name")));
                        }
                        if (this.plugin.getConfig().isSet("head-item.freeze-all-location.lore")) {
                            List<String> lore = new ArrayList<String>();
                            for (String loreLine : this.plugin.getConfig().getStringList("head-item.freeze-all-location.lore")) {
                                lore.add(this.plugin.placeholders(loreLine));
                            }
                            helmetMeta.setLore(lore);
                        }
                        newHelmet.setItemMeta(helmetMeta);
                    }
                    if (!this.helmetManager.similarToHelmetItem(oldFreezeAllLocationHelmet, newHelmet)) {
                        this.helmetManager.updateFreezeAllLocationHelmet(newHelmet);
                        this.helmetManager.replaceOldHelmets();
                    }

                    // CHANGE PARTICLES IF DIFFERENT
                    this.particleManager.setEffect(this.plugin.getConfig().getString("frozen-particles.particle", "null"));
                    this.particleManager.setRadius(this.plugin.getConfig().getInt("frozen-particles.radius", 10));

                    sender.sendMessage(this.plugin.placeholders("{PREFIX}Configuration file reloaded successfully"));
                    return true;
                }

                if (args[0].equalsIgnoreCase("locations")) {

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(this.plugin.placeholders("{PREFIX}You must be in-game to use &b/sf locations <set/remove> <location-name> [placeholder]"));
                        return false;
                    }

                    Player p = (Player) sender;
                    Location loc = p.getLocation();

                    if (args.length > 2) {
                        if (args[1].equalsIgnoreCase("set")) {
                            if (!sender.hasPermission("sf.locations.set")) {
                                for (String msg : this.plugin.getConfig().getStringList("no-permission-message")) {
                                    sender.sendMessage(this.plugin.placeholders(msg));
                                }
                                return false;
                            }

                            String location = args[2].toLowerCase();
                            String placeholder = args.length > 3 ? args[3] : null;
                            String path = "location-set";
                            if (this.plugin.getLocationsConfig().getConfig().isSet("locations." + location)) {
                                path = "location-updated";
                            }

                            this.plugin.getLocationsConfig().getConfig().set("locations." + location + ".placeholder", placeholder);
                            this.plugin.getLocationsConfig().getConfig().set("locations." + location + ".worldname", loc.getWorld().getName());
                            this.plugin.getLocationsConfig().getConfig().set("locations." + location + ".x", loc.getX());
                            this.plugin.getLocationsConfig().getConfig().set("locations." + location + ".y", loc.getY());
                            this.plugin.getLocationsConfig().getConfig().set("locations." + location + ".z", loc.getZ());
                            this.plugin.getLocationsConfig().getConfig().set("locations." + location + ".yaw", loc.getYaw());
                            this.plugin.getLocationsConfig().getConfig().set("locations." + location + ".pitch", loc.getPitch());
                            this.plugin.getLocationsConfig().saveConfig();
                            this.plugin.getLocationsConfig().reloadConfig();

                            for (String line : this.plugin.getConfig().getStringList(path)) {
                                sender.sendMessage(this.plugin.placeholders(line.replace("{LOCATION}", args[2])));
                            }
                            return true;
                        }

                        if (args[1].equalsIgnoreCase("remove")) {
                            if (!sender.hasPermission("sf.locations.remove")) {
                                for (String msg : this.plugin.getConfig().getStringList("no-permission-message")) {
                                    sender.sendMessage(this.plugin.placeholders(msg));
                                }
                                return false;
                            }

                            String location = args[2].toLowerCase();
                            if (!this.plugin.getLocationsConfig().getConfig().isSet("locations." + location)) {
                                for (String line : this.plugin.getConfig().getStringList("no-location-set")) {
                                    sender.sendMessage(this.plugin.placeholders(line.replace("{LOCATION}", args[2])));
                                }
                                return false;
                            }

                            this.plugin.getLocationsConfig().getConfig().set("locations." + location, null);
                            this.plugin.getLocationsConfig().saveConfig();
                            this.plugin.getLocationsConfig().reloadConfig();

                            for (String line : this.plugin.getConfig().getStringList("location-removed")) {
                                sender.sendMessage(this.plugin.placeholders(line.replace("{LOCATION}", args[2])));
                            }
                            return true;
                        }

                        sender.sendMessage(this.plugin.placeholders("{PREFIX}Invalid argument: &b" + args[1] + "&7, try &b/sf locations <set/remove> <location-name> [placeholder]"));
                        return false;
                    }

                    sender.sendMessage(this.plugin.placeholders("{PREFIX}Not enough arguments, try &b/sf locations <set/remove> <location-name> [placeholder]"));
                    return false;
                }
            }

            if (!sender.hasPermission("sf.help")) {
                for (String msg : this.plugin.getConfig().getStringList("no-permission-message")) {
                    sender.sendMessage(this.plugin.placeholders(msg));
                }
                return false;
            }

            sender.sendMessage(this.plugin.getHelpMessage());
            return true;
        }

        return false;
    }

}
