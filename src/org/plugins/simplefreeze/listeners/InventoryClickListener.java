package org.plugins.simplefreeze.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.GUIManager;
import org.plugins.simplefreeze.managers.PlayerManager;

import java.util.UUID;

public class InventoryClickListener implements Listener {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;
    private final GUIManager guiManager;

    public InventoryClickListener(SimpleFreezeMain plugin, PlayerManager playerManager, GUIManager guiManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        UUID uuid = e.getWhoClicked().getUniqueId();
        if (e.getClickedInventory() != null) {
            if (e.getClickedInventory().equals(e.getWhoClicked().getOpenInventory().getBottomInventory())) {
                if (e.getCurrentItem() != null) {
                    if (this.playerManager.isFrozen(uuid) && this.plugin.getConfig().isSet("head-item")) {
                        if (e.getSlot() == 39) {
                            for (String line : this.plugin.getConfig().getStringList("inventory-message")) {
                                e.getWhoClicked().sendMessage(this.plugin.placeholders(line));
                            }
                            e.setCancelled(true);
                        }
                    }
                    if (this.guiManager.containsPlayer(uuid) && e.isShiftClick()) {
                        e.setCancelled(true);
                    }
                }
            } else if (this.guiManager.containsPlayer(uuid)) {
                e.setCancelled(true);
            }
        }
    }
}
