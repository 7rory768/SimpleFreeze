package org.plugins.simplefreeze.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.objects.players.FrozenPlayer;
import org.plugins.simplefreeze.objects.players.TempFrozenPlayer;
import org.plugins.simplefreeze.util.FrozenType;
import org.plugins.simplefreeze.util.TimeUtil;

import java.util.*;

/**
 * Created by Rory on 4/27/2017.
 */
public class GUIManager {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;
    private final LocationManager locationManager;

    private String title;
    private int rows;
    private boolean guiEnabled;
    private boolean freezeAllGUIEnabled;
    private boolean allowedToClose;
    private Inventory skeletonInventory;
    private BukkitTask guiTask;
    private HashMap<UUID, Inventory> players = new HashMap<UUID, Inventory>();

    public GUIManager(SimpleFreezeMain plugin, PlayerManager playerManager, LocationManager locationManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.locationManager = locationManager;
        this.guiEnabled = this.plugin.getConfig().getBoolean("freeze-gui.enabled");
        this.freezeAllGUIEnabled = this.plugin.getConfig().getBoolean("freeze-gui.enabled-on-freezeall");
        this.allowedToClose = this.plugin.getConfig().getBoolean("freeze-gui.allow-close");
        this.rows = this.plugin.getConfig().getInt("freeze-gui.rows");
        this.title = this.plugin.getConfig().getString("freeze-gui.title");
        this.updateSkeletonInventory();
    }

    public void updateSkeletonInventory() {
        this.skeletonInventory = Bukkit.createInventory(null, 9 * this.rows, this.title);

        for (String itemKey : this.plugin.getConfig().getConfigurationSection("freeze-gui.items").getKeys(false)) {
            String path = "freeze-gui.items." + itemKey + ".";
            ItemStack item;
            Material material = null;
            try {
                material = material.valueOf(this.plugin.getConfig().getString(path + "material", "NULL"));
            } catch (IllegalArgumentException e) {
                continue;
            }
            int amount = this.plugin.getConfig().getInt(path + "amount", 1);
            short data = (short) this.plugin.getConfig().getInt(path + "data", 0);
            item = new ItemStack(material, amount, data);
            ItemMeta itemMeta = item.getItemMeta();
            if (this.plugin.getConfig().isSet(path + "name")) {
                itemMeta.setDisplayName(this.plugin.getConfig().getString(path + "name"));
            }

            if (this.plugin.getConfig().isSet(path + "lore")) {
                List<String> lore = new ArrayList<String>();
                for (String line : this.plugin.getConfig().getStringList(path + "lore")) {
                    lore.add(line);
                }
                itemMeta.setLore(lore);
            }

            item.setItemMeta(itemMeta);
            this.skeletonInventory.setItem(this.getSlot(this.plugin.getConfig().getInt(path + "x-cord"), this.plugin.getConfig().getInt(path + "y-cord")), item);
        }
    }

    public Inventory createPersonalGUI(UUID uuid) {
        if (this.playerManager.isFrozen(uuid)) {
            Inventory personalGUI = Bukkit.createInventory(null, 9 * this.rows, this.plugin.placeholders(this.title));
            FrozenPlayer frozenPlayer = this.playerManager.getFrozenPlayer(uuid);

            String freezee = frozenPlayer.getFreezeeName();
            String freezer = frozenPlayer.getFreezerName();
            String location = this.locationManager.getLocationPlaceholder(this.locationManager.getLocationName(frozenPlayer.getFreezeLoc()));
            String reason = frozenPlayer.getReason();
            String servers = this.plugin.getPlayerConfig().getConfig().getString("players." + uuid.toString() + ".servers", this.plugin.getServerID());

            for (int slot = 0; slot < this.skeletonInventory.getContents().length; slot++) {
                ItemStack item = this.skeletonInventory.getItem(slot);

                if (item != null) {
                    ItemMeta itemMeta = item.getItemMeta();
                    if (itemMeta.hasDisplayName()) {
                        itemMeta.setDisplayName(this.plugin.placeholders(itemMeta.getDisplayName().replace("{PLAYER}", freezee).replace("{FREEZER}", freezer).replace("{LOCATION}", location).replace("{REASON}", reason).replace("{SERVERS}", servers)));
                    }
                    if (itemMeta.hasLore()) {
                        List<String> lore = new ArrayList<String>();
                        for (String line : itemMeta.getLore()) {
                            lore.add(this.plugin.placeholders(line.replace("{PLAYER}", freezee).replace("{FREEZER}", freezer).replace("{LOCATION}", location).replace("{REASON}", reason).replace("{SERVERS}", servers)));
                        }
                        itemMeta.setLore(lore);
                    }
                    item.setItemMeta(itemMeta);
                }

                personalGUI.setItem(slot, item);
            }

            this.players.put(uuid, personalGUI);
            if (!this.guiTaskIsRunning()) {
                this.startGUIUpdateTask();
            }
            return refreshPersonalGUI(uuid);
        }
        return null;
    }

    public Inventory refreshPersonalGUI(UUID uuid) {
        Inventory personalGUI = this.players.get(uuid);
        if (this.playerManager.isFrozen(uuid) && personalGUI != null) {
            FrozenPlayer frozenPlayer = this.playerManager.getFrozenPlayer(uuid);
            String time = "Permanent";
            if (frozenPlayer instanceof TempFrozenPlayer) {
                time = TimeUtil.formatTime((((TempFrozenPlayer) frozenPlayer).getUnfreezeDate() - System.currentTimeMillis()) / 1000L);
            }
            for (int slot = 0; slot < personalGUI.getContents().length; slot++) {
                ItemStack item = personalGUI.getItem(slot);
                if (item != null) {
                    ItemMeta itemMeta = item.getItemMeta();
                    if (itemMeta.hasDisplayName()) {
                        itemMeta.setDisplayName(this.plugin.placeholders(itemMeta.getDisplayName().replace("{TIME}", time)));
                    }
                    if (itemMeta.hasLore()) {
                        List<String> lore = new ArrayList<String>();
                        for (String line : itemMeta.getLore()) {
                            lore.add(this.plugin.placeholders(line.replace("{TIME}", time)));
                        }
                        itemMeta.setLore(lore);
                    }
                    item.setItemMeta(itemMeta);

                }
            }
        }
        return personalGUI;
    }

    public int getSlot(int x, int y) {
        return (x - 1) + (y - 1) * 9;
    }

    public boolean containsPlayer(UUID uuid) {
        return this.players.containsKey(uuid);
    }

    public void removePlayer(UUID uuid) {
        this.players.remove(uuid);
        if (this.players.isEmpty()) {
            this.endGUIUpdateTask();
        }
    }

    public boolean isGUIEnabled() {
        return guiEnabled;
    }

    public void setGUIEnabled(boolean guiEnabled) {
        if (!this.guiEnabled && guiEnabled) {
            for (UUID uuid : this.playerManager.getFrozenPlayers().keySet()) {
                if (!this.playerManager.isFreezeAllFrozen(uuid) || this.playerManager.isFreezeAllFrozen(uuid) && this.isFreezeAllGUIEnabled()) {
                    Inventory gui = createPersonalGUI(uuid);
                    Bukkit.getPlayer(uuid).openInventory(gui);
                    this.players.put(uuid, gui);
                }
            }
            if (!this.guiTaskIsRunning()) {
                this.startGUIUpdateTask();
            }
        } else if (this.guiEnabled && !guiEnabled) {
            for (UUID uuid : this.players.keySet()) {
                Bukkit.getPlayer(uuid).closeInventory();
            }
        }
        this.guiEnabled = guiEnabled;
        if (this.guiTaskIsRunning()) {
            this.endGUIUpdateTask();
        }
    }

    public boolean isFreezeAllGUIEnabled() {
        return freezeAllGUIEnabled;
    }

    public void setFreezeAllGUIEnabled(boolean freezeAllGUIEnabled) {
        if (!this.freezeAllGUIEnabled && freezeAllGUIEnabled) {
            for (UUID uuid : this.playerManager.getFrozenPlayers().keySet()) {
                if (this.playerManager.isFreezeAllFrozen(uuid)) {
                    Inventory gui = createPersonalGUI(uuid);
                    Bukkit.getPlayer(uuid).openInventory(gui);
                    this.players.put(uuid, gui);
                }
            }
        } else if (this.freezeAllGUIEnabled && !freezeAllGUIEnabled) {
            for (UUID uuid : this.playerManager.getFrozenPlayers().keySet()) {
                if (this.playerManager.isFreezeAllFrozen(uuid)) {
                    Bukkit.getPlayer(uuid).closeInventory();
                    this.players.remove(uuid);
                }
            }
        }
        this.freezeAllGUIEnabled = freezeAllGUIEnabled;
    }

    public boolean isAllowedToClose() {
        return allowedToClose;
    }

    public void setAllowedToClose(boolean allowedToClose) {
        this.allowedToClose = allowedToClose;
    }

    public boolean guiTaskIsRunning() {
        return this.guiTask != null;
    }

    public void startGUIUpdateTask() {
        this.guiTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : players.keySet()) {
                    if (playerManager.isFrozen(uuid)) {
                        if (playerManager.getFrozenPlayer(uuid).getType() == FrozenType.TEMP_FROZEN) {
                            Bukkit.getPlayer(uuid).openInventory(refreshPersonalGUI(uuid));
                        }
                    }
                }
            }
        }.runTaskTimer(this.plugin, 20L, 20L);
    }

    public void endGUIUpdateTask() {
        if (this.guiTask != null) {
            this.guiTask.cancel();
            this.guiTask = null;
        }
    }


}
