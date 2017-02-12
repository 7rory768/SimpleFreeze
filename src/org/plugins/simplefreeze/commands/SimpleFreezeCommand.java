package org.plugins.simplefreeze.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.cache.FrozenPages;
import org.plugins.simplefreeze.managers.FreezeManager;
import org.plugins.simplefreeze.managers.HelmetManager;
import org.plugins.simplefreeze.managers.ParticleManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SimpleFreezeCommand implements CommandExecutor {

    private final SimpleFreezeMain plugin;
    private final FreezeManager freezeManager;
    private final HelmetManager helmetManager;
    private final FrozenPages frozenPages;
    private final ParticleManager particleManager;

    public SimpleFreezeCommand(SimpleFreezeMain plugin, FreezeManager freezeManager, HelmetManager helmetManager, FrozenPages frozenPages, ParticleManager particleManager) {
        this.plugin = plugin;
        this.freezeManager = freezeManager;
        this.helmetManager = helmetManager;
        this.frozenPages = frozenPages;
        this.particleManager = particleManager;
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

                    // STORE OLD /FROZEN FORMATS
                    String frozenStr = this.plugin.getConfig().getString("frozen-list-format.formats.frozen");
                    String frozenLocStr = this.plugin.getConfig().getString("frozen-list-format.formats.frozen-location");
                    String tempFrozenStr = this.plugin.getConfig().getString("frozen-list-format.formats.temp-frozen");
                    String tempFrozenLocStr = this.plugin.getConfig().getString("frozen-list-format.formats.temp-frozen-location");
                    String onlinePlaceholder = this.plugin.getConfig().getString("frozen-list-format.online-placeholder");
                    String offlinePlaceholder = this.plugin.getConfig().getString("frozen-list-format.offline-placeholder");

                    HashSet<String> strings = new HashSet<String>();

                    this.plugin.reloadConfig();
                    this.plugin.updateFinalPrefixFormatting();

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
