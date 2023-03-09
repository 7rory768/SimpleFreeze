package org.plugins.simplefreeze.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.GUIManager;

import java.util.UUID;

/**
 * Created by Rory on 4/30/2017.
 */
public class InventoryCloseListener implements Listener {

    private final SimpleFreezeMain plugin;
    private final GUIManager guiManager;

    public InventoryCloseListener(SimpleFreezeMain plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void inventoryClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        Inventory inventory = e.getInventory();
        UUID uuid = p.getUniqueId();
        if (p != null && p.isOnline()) {
            if (this.guiManager.isGUIEnabled() && this.guiManager.containsPlayer(uuid)) {
                if (!this.guiManager.isAllowedToClose()) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (inventory != null && inventory.getType() != InventoryType.CRAFTING) {
                                p.openInventory(inventory);
                            }
                        }
                    }.runTaskLater(this.plugin, 1L);

                } else {
                    this.guiManager.removePlayer(uuid);
                }
            }
        }
    }

}
