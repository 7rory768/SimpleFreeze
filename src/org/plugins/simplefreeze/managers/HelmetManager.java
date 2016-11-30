package org.plugins.simplefreeze.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.objects.FrozenPlayer;
import org.plugins.simplefreeze.objects.TempFrozenPlayer;
import org.plugins.simplefreeze.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rory on 11/29/2016.
 */
public class HelmetManager {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;
    private final LocationManager locationManager;

    private ItemStack helmetItem = null;
    private boolean helmetUsedToBeNull = false;

    public HelmetManager(SimpleFreezeMain plugin, PlayerManager playerManager, LocationManager locationManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        this.setupHelmetItem();
    }

    private void setupHelmetItem() {
        ItemStack helmetItem = null;
        if (this.plugin.getConfig().isSet("head-item.material")) {
            short data = this.plugin.getConfig().isSet("head-item.data") ? (short) this.plugin.getConfig().getInt("head-item.data") : 0;
            helmetItem = new ItemStack(Material.getMaterial(this.plugin.getConfig().getString("head-item.material")), 1, data);
            ItemMeta helmetMeta = helmetItem.getItemMeta();
            if (this.plugin.getConfig().isSet("head-item.name")) {
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
        }
        this.helmetItem = helmetItem;
    }

    public void updateHelmetItem(ItemStack newHelmetItem) {
        if (this.helmetItem == null) {
            this.helmetUsedToBeNull = true;
        }
        this.helmetItem = newHelmetItem;
    }

    public boolean similarToHelmetItem(ItemStack newHelmetItem) {
        if (newHelmetItem == null && this.helmetItem == null) {
            return true;
        } else if ((newHelmetItem == null && this.helmetItem != null) || (newHelmetItem != null && this.helmetItem == null)) {
            return false;
        }
        return this.helmetItem.isSimilar(newHelmetItem);
    }

    public void replaceOldHelmets() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (this.playerManager.isFrozen(p)) {
                if (this.helmetUsedToBeNull && p.getInventory().getHelmet() != null) {
                    this.playerManager.getFrozenPlayer(p).setHelmet(p.getInventory().getHelmet());
                }
                if (this.helmetItem == null && !this.helmetUsedToBeNull) {
                    p.getInventory().setHelmet(this.playerManager.getFrozenPlayer(p).getHelmet());
                } else {
                    p.getInventory().setHelmet(this.helmetItem);
                }
            }
        }
        this.helmetUsedToBeNull = false;
    }

    public ItemStack getPersonalHelmetItem(FrozenPlayer frozenPlayer) {
        String playerPlaceholder = frozenPlayer.getFreezeeName();
        String freezerPlaceholder = frozenPlayer.getFreezerName();
        String locName = this.locationManager.getLocationName(frozenPlayer.getFreezeLoc());
        String locationPlaceholder = locName == null ? this.plugin.getConfig().getString("location") : this.plugin.getConfig().getString("locations." + locName + ".placeholder", locName);
        String timePlaceholder = "";
        String serversPlaceholder = "";
        if (frozenPlayer instanceof TempFrozenPlayer) {
            timePlaceholder = TimeUtil.formatTime((((TempFrozenPlayer) frozenPlayer).getUnfreezeDate() - frozenPlayer.getFreezeDate())/1000L);
        }
        ItemStack helmetItem = this.helmetItem == null ? null : this.helmetItem.clone();
        if (helmetItem != null) {
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
        }
        return helmetItem;
    }

    public ItemStack getPersonalHelmetItem(String freezeeName, String freezerName, String location, Long time) {
        String playerPlaceholder = freezeeName;
        String freezerPlaceholder = freezerName;
        String locationPlaceholder = location == null ? this.plugin.getConfig().getString("location") : this.plugin.getConfig().getString("locations." + location + ".placeholder", location);
        String timePlaceholder = "";
        String serversPlaceholder = "";
        if (time != null) {
            timePlaceholder = TimeUtil.formatTime(time);
        }
        ItemStack helmetItem = this.helmetItem == null ? null : this.helmetItem.clone();
        if (helmetItem != null) {
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
        }
        return helmetItem;
    }
}
