package org.plugins.simplefreeze.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.FreezeManager;

import java.util.ArrayList;
import java.util.List;

public class SimpleFreezeCommand implements CommandExecutor {

    private final SimpleFreezeMain plugin;
    private final FreezeManager freezeManager;

    public SimpleFreezeCommand(SimpleFreezeMain plugin, FreezeManager freezeManager) {
        this.plugin = plugin;
        this.freezeManager = freezeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("simplefreeze")) {

            if (args.length > 0) {

                if (args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("simplefreeze.reload")) {
                        sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("no-permission-message")));
                        return false;
                    }

                    // REPLACE HELMETS IF THEY CHANGED,
                    // CHANGE PARTICLES IF DIFFERENT

                    this.plugin.reloadConfig();
                    this.plugin.updateFinalPrefixFormatting();
                    ItemStack newHelmetItem = null;
                    if (this.plugin.getConfig().isSet("head-item.material")) {
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
                    if (!this.freezeManager.similarToHelmetItem(newHelmetItem)) {
                        this.freezeManager.updateHelmetItem(newHelmetItem);
                        this.freezeManager.replaceOldHelmets();
                    }
                    sender.sendMessage(this.plugin.placeholders("{PREFIX}Configuration file reloaded successfully"));
                    return true;
                }

                if (!sender.hasPermission("simplefreeze.help")) {
                    sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("no-permission-message")));
                    return false;
                }

                sender.sendMessage(this.plugin.getHelpMessage());
                return true;
            }

            if (!sender.hasPermission("simplefreeze.help")) {
                sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("no-permission-message")));
                return false;
            }

            sender.sendMessage(this.plugin.getHelpMessage());
            return true;
        }

        return false;
    }

}
