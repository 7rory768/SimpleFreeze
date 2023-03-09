package org.plugins.simplefreeze.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.plugins.simplefreeze.managers.GUIManager;

/**
 * Created by Rory on 4/30/2017.
 */
public class InventoryDragListener implements Listener {

    private final GUIManager guiManager;

    public InventoryDragListener(GUIManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (this.guiManager.containsPlayer(p.getUniqueId()) && p.getOpenInventory().getTopInventory().equals(e.getInventory())) {
            e.setCancelled(true);
        }
    }
}
