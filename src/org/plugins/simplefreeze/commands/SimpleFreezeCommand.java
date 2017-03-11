package org.plugins.simplefreeze.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.cache.FrozenPages;
import org.plugins.simplefreeze.managers.HelmetManager;
import org.plugins.simplefreeze.managers.ParticleManager;
import org.plugins.simplefreeze.managers.SoundManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SimpleFreezeCommand implements CommandExecutor {

    private final SimpleFreezeMain plugin;
    private final HelmetManager helmetManager;
    private final FrozenPages frozenPages;
    private final ParticleManager particleManager;
    private final SoundManager soundManager;

    public SimpleFreezeCommand(SimpleFreezeMain plugin, HelmetManager helmetManager, FrozenPages frozenPages, ParticleManager particleManager, SoundManager soundManager) {
        this.plugin = plugin;
        this.helmetManager = helmetManager;
        this.frozenPages = frozenPages;
        this.particleManager = particleManager;
        this.soundManager = soundManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("simplefreeze")) {

            if (args.length > 0) {

                if (args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("sf.reload")) {
                        for (String msg : this.plugin.getConfig().getStringList("no-permission-message")) {
                            if (!msg.equals("")) {
                                sender.sendMessage(this.plugin.placeholders(msg));
                            }
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

                    HashSet<String> strings = new HashSet<String>();

                    // CHECK IF /FROZEN FORMATS CHANGED
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

                if (!sender.hasPermission("sf.help")) {
                    sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("no-permission-message")));
                    return false;
                }

                sender.sendMessage(this.plugin.getHelpMessage());
                return true;
            }

            if (!sender.hasPermission("sf.help")) {
                sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("no-permission-message")));
                return false;
            }

            sender.sendMessage(this.plugin.getHelpMessage());
            return true;
        }

        return false;
    }

}
