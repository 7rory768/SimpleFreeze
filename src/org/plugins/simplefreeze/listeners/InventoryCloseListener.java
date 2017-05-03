package org.plugins.simplefreeze.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
        UUID uuid = e.getPlayer().getUniqueId();
        if (this.guiManager.isGUIEnabled() && this.guiManager.containsPlayer(uuid)) {
            if (!this.guiManager.isAllowedToClose()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        e.getPlayer().openInventory(guiManager.refreshPersonalGUI(uuid));
                    }
                }.runTaskLater(this.plugin, 0L);

            } else {
                this.guiManager.removePlayer(uuid);
            }
        }
    }

}
