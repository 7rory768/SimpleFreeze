package org.plugins.simplefreeze.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.objects.FreezeAllPlayer;
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

    private ItemStack frozenHelmet = null;
    private ItemStack frozenLocationHelmet = null;
    private ItemStack tempFrozenHelmet = null;
    private ItemStack tempFrozenLocationHelmet = null;
    private ItemStack freezeAllHelmet = null;
    private ItemStack freezeAllLocationHelmet = null;

    private boolean frozenHelmetWasNull = false;
    private boolean frozenLocationHelmetWasNull = false;
    private boolean tempFrozenHelmetWasNull = false;
    private boolean tempFrozenLocationHelmetWasNull = false;
    private boolean freezeAllHelmetWasNull = false;
    private boolean freezeAllLocationHelmetWasNull = false;

    private BukkitTask helmetUpdateTask = null;

    public HelmetManager(SimpleFreezeMain plugin, PlayerManager playerManager, LocationManager locationManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        this.setupHelmetItems();
    }

    private void setupHelmetItems() {
        this.setupFrozenHelmet();
        this.setupFrozenLocationHelmet();
        this.setupTempFrozenHelmet();
        this.setupTempFrozenLocationHelmet();
        this.setupFreezeAllHelmet();
        this.setupFreezeAllLocationHelmet();
    }

    public void setupFrozenHelmet() {
        ItemStack helmetItem = null;
        if (this.plugin.getConfig().isSet("head-item.frozen.material")) {
            short data = this.plugin.getConfig().isSet("head-item.frozen.data") ? (short) this.plugin.getConfig().getInt("head-item.frozen.data") : 0;
            helmetItem = new ItemStack(Material.getMaterial(this.plugin.getConfig().getString("head-item.frozen.material")), 1, data);
            ItemMeta helmetMeta = helmetItem.getItemMeta();
            if (this.plugin.getConfig().isSet("head-item.frozen.name")) {
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
            helmetItem.setItemMeta(helmetMeta);
        }
        this.frozenHelmet = helmetItem;
    }

    public void setupFrozenLocationHelmet() {
        ItemStack helmetItem = null;
        if (this.plugin.getConfig().isSet("head-item.frozen-location.material")) {
            short data = this.plugin.getConfig().isSet("head-item.frozen-location.data") ? (short) this.plugin.getConfig().getInt("head-item.frozen-location.data") : 0;
            helmetItem = new ItemStack(Material.getMaterial(this.plugin.getConfig().getString("head-item.frozen-location.material")), 1, data);
            ItemMeta helmetMeta = helmetItem.getItemMeta();
            if (this.plugin.getConfig().isSet("head-item.frozen-location.name")) {
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
            helmetItem.setItemMeta(helmetMeta);
        }
        this.frozenLocationHelmet = helmetItem;
        if (helmetItem == null) {
            this.frozenLocationHelmet = this.frozenHelmet;
        }
    }

    public void setupTempFrozenHelmet() {
        ItemStack helmetItem = null;
        if (this.plugin.getConfig().isSet("head-item.temp-frozen.material")) {
            short data = this.plugin.getConfig().isSet("head-item.temp-frozen.data") ? (short) this.plugin.getConfig().getInt("head-item.temp-frozen.data") : 0;
            helmetItem = new ItemStack(Material.getMaterial(this.plugin.getConfig().getString("head-item.temp-frozen.material")), 1, data);
            ItemMeta helmetMeta = helmetItem.getItemMeta();
            if (this.plugin.getConfig().isSet("head-item.temp-frozen.name")) {
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
            helmetItem.setItemMeta(helmetMeta);
        }
        this.tempFrozenHelmet = helmetItem;
        if (helmetItem == null) {
            this.tempFrozenHelmet = this.frozenHelmet;
        }

        if (this.tempFrozenHelmet != null && !this.helmetTaskIsRunning()) {
            if (this.tempFrozenHelmet.getItemMeta().hasDisplayName()) {
                if (this.tempFrozenHelmet.getItemMeta().getDisplayName().contains("{TIME}")) {
                    this.startHelmetUpdateTask();
                }
            }
            if (this.tempFrozenHelmet.getItemMeta().hasLore()) {
                if (this.tempFrozenHelmet.getItemMeta().getLore().toString().contains("{TIME}")) {
                    this.startHelmetUpdateTask();
                }
            }
        }
    }

    public void setupTempFrozenLocationHelmet() {
        ItemStack helmetItem = null;
        if (this.plugin.getConfig().isSet("head-item.temp-frozen-location.material")) {
            short data = this.plugin.getConfig().isSet("head-item.temp-frozen-location.data") ? (short) this.plugin.getConfig().getInt("head-item.temp-frozen-location.data") : 0;
            helmetItem = new ItemStack(Material.getMaterial(this.plugin.getConfig().getString("head-item.temp-frozen-location.material")), 1, data);
            ItemMeta helmetMeta = helmetItem.getItemMeta();
            if (this.plugin.getConfig().isSet("head-item.temp-frozen-location.name")) {
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
            helmetItem.setItemMeta(helmetMeta);
        }
        this.tempFrozenLocationHelmet = helmetItem;
        if (helmetItem == null) {
            this.tempFrozenLocationHelmet = this.frozenHelmet;
        }
        if (this.tempFrozenLocationHelmet != null && !this.helmetTaskIsRunning()) {
            if (this.tempFrozenLocationHelmet.getItemMeta().hasDisplayName()) {
                if (this.tempFrozenLocationHelmet.getItemMeta().getDisplayName().contains("{TIME}")) {
                    this.startHelmetUpdateTask();
                }
            }
            if (this.tempFrozenLocationHelmet.getItemMeta().hasLore()) {
                if (this.tempFrozenLocationHelmet.getItemMeta().getLore().toString().contains("{TIME}")) {
                    this.startHelmetUpdateTask();
                }
            }
        }
    }

    public void setupFreezeAllHelmet() {
        ItemStack helmetItem = null;
        if (this.plugin.getConfig().isSet("head-item.freeze-all.material")) {
            short data = this.plugin.getConfig().isSet("head-item.data") ? (short) this.plugin.getConfig().getInt("head-item.freeze-all.data") : 0;
            helmetItem = new ItemStack(Material.getMaterial(this.plugin.getConfig().getString("head-item.freeze-all.material")), 1, data);
            ItemMeta helmetMeta = helmetItem.getItemMeta();
            if (this.plugin.getConfig().isSet("head-item.freeze-all.name")) {
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
            helmetItem.setItemMeta(helmetMeta);
        }
        this.freezeAllHelmet = helmetItem;
        if (helmetItem == null) {
            this.freezeAllHelmet = this.frozenHelmet;
        }
    }

    public void setupFreezeAllLocationHelmet() {
        ItemStack helmetItem = null;
        if (this.plugin.getConfig().isSet("head-item.freeze-all-location.material")) {
            short data = this.plugin.getConfig().isSet("head-item.freeze-all-location.data") ? (short) this.plugin.getConfig().getInt("head-item.freeze-all-location.data") : 0;
            helmetItem = new ItemStack(Material.getMaterial(this.plugin.getConfig().getString("head-item.freeze-all-location.material")), 1, data);
            ItemMeta helmetMeta = helmetItem.getItemMeta();
            if (this.plugin.getConfig().isSet("head-item.freeze-all-location.name")) {
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
            helmetItem.setItemMeta(helmetMeta);
        }
        this.freezeAllLocationHelmet = helmetItem;
        if (helmetItem == null) {
            this.freezeAllLocationHelmet = this.frozenHelmet;
        }
    }

    public void updateFrozenHelmet(ItemStack newFrozenHelmet) {
        if (this.frozenHelmet == null) {
            this.frozenHelmetWasNull = true;
        }

        if (this.frozenLocationHelmet == this.frozenHelmet) {
            this.frozenLocationHelmet = newFrozenHelmet;
        }
        if (this.tempFrozenHelmet == this.frozenHelmet) {
            this.tempFrozenHelmet = newFrozenHelmet;
        }
        if (this.tempFrozenLocationHelmet == this.frozenHelmet) {
            this.tempFrozenLocationHelmet = newFrozenHelmet;
        }
        if (this.freezeAllHelmet == this.frozenHelmet) {
            this.freezeAllHelmet = newFrozenHelmet;
        }
        if (this.freezeAllLocationHelmet == this.frozenHelmet) {
            this.freezeAllLocationHelmet = newFrozenHelmet;
        }

        this.frozenHelmet = newFrozenHelmet;
        if (newFrozenHelmet == null && !this.frozenHelmetWasNull && this.helmetTaskIsRunning()) {
            this.endHelmetUpdateTask();
        } else if (this.frozenHelmetWasNull && this.frozenHelmet != null && !this.helmetTaskIsRunning()) {
            if (this.frozenHelmet.getItemMeta().hasLore()) {
                this.startHelmetUpdateTask();
            }
        }

    }

    public void updateFrozenLocationHelmet(ItemStack newFrozenLocationHelmet) {
        if (this.frozenLocationHelmet == null) {
            this.frozenLocationHelmetWasNull = true;
        }
        this.frozenLocationHelmet = newFrozenLocationHelmet;
        if (newFrozenLocationHelmet == null) {
            this.frozenLocationHelmet = this.frozenHelmet;
        }
    }

    public void updateTempFrozenHelmet(ItemStack newTempFrozenHelmet) {
        if (this.tempFrozenHelmet == null) {
            this.tempFrozenHelmetWasNull = true;
        }
        this.tempFrozenHelmet = newTempFrozenHelmet;
        if (newTempFrozenHelmet == null && !this.tempFrozenHelmetWasNull && this.helmetTaskIsRunning()) {
            this.endHelmetUpdateTask();
        }
        if (this.tempFrozenHelmetWasNull && this.tempFrozenHelmet != null && !this.helmetTaskIsRunning()) {
            if (this.tempFrozenHelmet.getItemMeta().hasLore()) {
                this.startHelmetUpdateTask();
            }
        }
        if (newTempFrozenHelmet == null) {
            this.tempFrozenHelmet = this.frozenHelmet;
        }
    }

    public void updateTempFrozenLocationHelmet(ItemStack newTempFrozenLocationHelmet) {
        if (this.tempFrozenLocationHelmet == null) {
            this.tempFrozenLocationHelmetWasNull = true;
        }
        this.tempFrozenLocationHelmet = newTempFrozenLocationHelmet;
        if (newTempFrozenLocationHelmet == null && !this.tempFrozenLocationHelmetWasNull && this.helmetTaskIsRunning()) {
            this.endHelmetUpdateTask();
        }
        if (this.tempFrozenLocationHelmetWasNull && this.tempFrozenLocationHelmet != null && !this.helmetTaskIsRunning()) {
            if (this.tempFrozenLocationHelmet.getItemMeta().hasLore()) {
                this.startHelmetUpdateTask();
            }
        }
        if (newTempFrozenLocationHelmet == null) {
            this.tempFrozenLocationHelmet = this.frozenHelmet;
        }
    }

    public void updateFreezeAllHelmet(ItemStack newFreezeAllHelmet) {
        if (this.freezeAllHelmet == null) {
            this.freezeAllHelmetWasNull = true;
        }
        this.freezeAllHelmet = newFreezeAllHelmet;
        if (newFreezeAllHelmet == null) {
            this.freezeAllHelmet = this.frozenHelmet;
        }
    }

    public void updateFreezeAllLocationHelmet(ItemStack newFreezeAllLocationHelmet) {
        if (this.freezeAllLocationHelmet == null) {
            this.freezeAllLocationHelmetWasNull = true;
        }
        this.freezeAllLocationHelmet = newFreezeAllLocationHelmet;
        if (newFreezeAllLocationHelmet == null) {
            this.freezeAllLocationHelmet = this.frozenHelmet;
        }
    }

    public boolean similarToHelmetItem(ItemStack oldHelmet, ItemStack newHelmet) {
        if (newHelmet == null && oldHelmet == null) {
            return true;
        } else if ((newHelmet == null && oldHelmet != null) || (newHelmet != null && oldHelmet == null)) {
            return false;
        }
        return oldHelmet.isSimilar(newHelmet);
    }

    public void replaceOldHelmets() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (this.playerManager.isFrozen(p)) {
                FrozenPlayer frozenPlayer = this.playerManager.getFrozenPlayer(p);
                boolean location = this.locationManager.getLocationName(frozenPlayer.getFreezeLoc()) != null;
                if (frozenPlayer instanceof TempFrozenPlayer) {
                    if (location) {
                        if (this.tempFrozenLocationHelmetWasNull && p.getInventory().getHelmet() != null) {
                            this.playerManager.getFrozenPlayer(p).setHelmet(p.getInventory().getHelmet());
                        }
                        if (this.tempFrozenLocationHelmet == null && !this.tempFrozenLocationHelmetWasNull) {
                            p.getInventory().setHelmet(this.playerManager.getFrozenPlayer(p).getHelmet());
                        } else {
                            p.getInventory().setHelmet(this.getPersonalHelmetItem(frozenPlayer));
                        }
                    } else {
                        if (this.tempFrozenHelmetWasNull && p.getInventory().getHelmet() != null) {
                            this.playerManager.getFrozenPlayer(p).setHelmet(p.getInventory().getHelmet());
                        }
                        if (this.tempFrozenHelmet == null && !this.tempFrozenHelmetWasNull) {
                            p.getInventory().setHelmet(this.playerManager.getFrozenPlayer(p).getHelmet());
                        } else {
                            p.getInventory().setHelmet(this.getPersonalHelmetItem(frozenPlayer));
                        }
                    }
                } else if (frozenPlayer instanceof FreezeAllPlayer) {
                    if (location) {
                        if (this.freezeAllLocationHelmetWasNull && p.getInventory().getHelmet() != null) {
                            this.playerManager.getFrozenPlayer(p).setHelmet(p.getInventory().getHelmet());
                        }
                        if (this.freezeAllLocationHelmet == null && !this.freezeAllLocationHelmetWasNull) {
                            p.getInventory().setHelmet(this.playerManager.getFrozenPlayer(p).getHelmet());
                        } else {
                            p.getInventory().setHelmet(this.getPersonalHelmetItem(frozenPlayer));
                        }
                    } else {
                        if (this.freezeAllHelmetWasNull && p.getInventory().getHelmet() != null) {
                            this.playerManager.getFrozenPlayer(p).setHelmet(p.getInventory().getHelmet());
                        }
                        if (this.freezeAllHelmet == null && !this.freezeAllHelmetWasNull) {
                            p.getInventory().setHelmet(this.playerManager.getFrozenPlayer(p).getHelmet());
                        } else {
                            p.getInventory().setHelmet(this.getPersonalHelmetItem(frozenPlayer));
                        }
                    }
                } else if (location) {
                    if (this.frozenLocationHelmetWasNull && p.getInventory().getHelmet() != null) {
                        this.playerManager.getFrozenPlayer(p).setHelmet(p.getInventory().getHelmet());
                    }
                    if (this.frozenLocationHelmet == null && !this.frozenLocationHelmetWasNull) {
                        p.getInventory().setHelmet(this.playerManager.getFrozenPlayer(p).getHelmet());
                    } else {
                        p.getInventory().setHelmet(this.getPersonalHelmetItem(frozenPlayer));
                    }
                } else {
                    if (this.frozenHelmetWasNull && p.getInventory().getHelmet() != null) {
                        this.playerManager.getFrozenPlayer(p).setHelmet(p.getInventory().getHelmet());
                    }
                    if (this.frozenHelmet == null && !this.frozenHelmetWasNull) {
                        p.getInventory().setHelmet(this.playerManager.getFrozenPlayer(p).getHelmet());
                    } else {
                        p.getInventory().setHelmet(this.getPersonalHelmetItem(frozenPlayer));
                    }
                }
            }
        }
        this.frozenHelmetWasNull = false;
        this.frozenLocationHelmetWasNull = false;
        this.tempFrozenHelmetWasNull = false;
        this.tempFrozenLocationHelmetWasNull = false;
        this.freezeAllHelmetWasNull = false;
        this.freezeAllLocationHelmetWasNull = false;
    }

    public ItemStack getPersonalHelmetItem(FrozenPlayer frozenPlayer) {
        String playerPlaceholder = frozenPlayer.getFreezeeName();
        String freezerPlaceholder = frozenPlayer.getFreezerName();
        String locName = this.locationManager.getLocationName(frozenPlayer.getFreezeLoc());
        String locationPlaceholder = this.locationManager.getLocationPlaceholder(locName);
        String timePlaceholder = "";
        String serversPlaceholder = this.plugin.getConfig().getString("players." + frozenPlayer.getFreezeeUUID() + ".servers", "");
        String reasonPlaceholder = frozenPlayer.getReason();

        ItemStack helmetItem = null;

        if (frozenPlayer instanceof TempFrozenPlayer) {
            timePlaceholder = TimeUtil.formatTime((((TempFrozenPlayer) frozenPlayer).getUnfreezeDate() - System.currentTimeMillis()) / 1000L);
            if (locName == null) {
                helmetItem = this.tempFrozenHelmet == null ? null : this.tempFrozenHelmet.clone();
            } else {
                helmetItem = this.tempFrozenLocationHelmet == null ? null : this.tempFrozenLocationHelmet.clone();
            }
        } else if (frozenPlayer instanceof FreezeAllPlayer) {
            if (locName == null) {
                helmetItem = this.freezeAllHelmet == null ? null : this.freezeAllHelmet.clone();
            } else {
                helmetItem = this.freezeAllLocationHelmet == null ? null : this.freezeAllLocationHelmet.clone();
            }
        } else if (locName == null) {
            helmetItem = this.frozenHelmet == null ? null : this.frozenHelmet.clone();
        } else {
            helmetItem = this.frozenLocationHelmet == null ? null : this.frozenLocationHelmet.clone();
        }

        if (helmetItem != null) {
            ItemMeta helmetMeta = helmetItem.getItemMeta();
            if (helmetMeta.hasDisplayName()) {
                // More placeholders should be added here (location, freezer, time)
                // Also allow enchants and itemflags
                helmetMeta.setDisplayName(this.plugin.placeholders(helmetMeta.getDisplayName().replace("{FREEZER}", freezerPlaceholder).replace("{PLAYER}", playerPlaceholder).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reasonPlaceholder)));
            }
            if (helmetMeta.hasLore()) {
                List<String> lore = new ArrayList<String>();
                for (String loreLine : helmetMeta.getLore()) {
                    lore.add(this.plugin.placeholders(loreLine.replace("{FREEZER}", freezerPlaceholder).replace("{PLAYER}", playerPlaceholder).replace("{TIME}", timePlaceholder)
                            .replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", serversPlaceholder).replace("{REASON}", reasonPlaceholder)));
                }
                helmetMeta.setLore(lore);
            }
            helmetItem.setItemMeta(helmetMeta);
        }

        return helmetItem;
    }

    public boolean helmetTaskIsRunning() {
        return this.helmetUpdateTask != null;
    }

    public void startHelmetUpdateTask() {
        this.helmetUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (FrozenPlayer frozenPlayer : playerManager.getFrozenPlayers().values()) {
                    if (frozenPlayer instanceof TempFrozenPlayer) {
                        Bukkit.getPlayer(frozenPlayer.getFreezeeUUID()).getInventory().setHelmet(getPersonalHelmetItem(frozenPlayer));
                    }
                }
            }
        }.runTaskTimer(this.plugin, 20L, 20L);
    }

    public void endHelmetUpdateTask() {
        if (this.helmetUpdateTask != null) {
            this.helmetUpdateTask.cancel();
            this.helmetUpdateTask = null;
        }
    }

    public ItemStack getFrozenHelmet() {
        return this.frozenHelmet;
    }

    public ItemStack getFrozenLocationHelmet() {
        return this.frozenLocationHelmet;
    }

    public ItemStack getTempFrozenHelmet() {
        return this.tempFrozenHelmet;
    }

    public ItemStack getTempFrozenLocationHelmet() {
        return this.tempFrozenLocationHelmet;
    }

    public ItemStack getFreezeAllHelmet() {
        return this.freezeAllHelmet;
    }

    public ItemStack getFreezeAllLocationHelmet() {
        return this.freezeAllLocationHelmet;
    }
}