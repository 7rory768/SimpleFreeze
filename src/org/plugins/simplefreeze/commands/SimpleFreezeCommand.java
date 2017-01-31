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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SimpleFreezeCommand implements CommandExecutor {

    private final SimpleFreezeMain plugin;
    private final FreezeManager freezeManager;
    private final HelmetManager helmetManager;
    private final FrozenPages frozenPages;

    public SimpleFreezeCommand(SimpleFreezeMain plugin, FreezeManager freezeManager, HelmetManager helmetManager, FrozenPages frozenPages) {
        this.plugin = plugin;
        this.freezeManager = freezeManager;
        this.helmetManager = helmetManager;
        this.frozenPages = frozenPages;
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

                    this.plugin.reloadConfig();
                    this.plugin.updateFinalPrefixFormatting();

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
                    // CHANGE PARTICLES IF DIFFERENT
                    ItemStack newHelmetItem = null;
                    if (this.plugin.getConfig().isSet("head-item")) {
                        short data = this.plugin.getConfig().isSet("head-item.data") ? (short) this.plugin.getConfig().getInt("head-item.data") : 0;
                        newHelmetItem = new ItemStack(Material.getMaterial(this.plugin.getConfig().getString("head-item.material")), 1, data);
                        ItemMeta helmetMeta = newHelmetItem.getItemMeta();
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
                        newHelmetItem.setItemMeta(helmetMeta);
                    }
                    if (!this.helmetManager.similarToHelmetItem(newHelmetItem)) {
                        this.helmetManager.updateHelmetItem(newHelmetItem);
                        this.helmetManager.replaceOldHelmets();
                    }

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
