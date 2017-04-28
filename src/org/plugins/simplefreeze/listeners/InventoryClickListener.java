package org.plugins.simplefreeze.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.PlayerManager;

public class InventoryClickListener implements Listener {

	private final SimpleFreezeMain plugin;
	private final PlayerManager playerManager;

	public InventoryClickListener(SimpleFreezeMain plugin, PlayerManager playerManager) {
		this.plugin = plugin;
		this.playerManager = playerManager;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (this.playerManager.isFrozen(e.getWhoClicked().getUniqueId()) && this.plugin.getConfig().isSet("head-item")) {
			if (e.getClickedInventory() != null && e.getSlot() == 39 && e.getCurrentItem() != null) {
				if (e.getClickedInventory().equals(e.getWhoClicked().getOpenInventory().getBottomInventory())) {
					for (String line : this.plugin.getConfig().getStringList("inventory-message")) {
						e.getWhoClicked().sendMessage(this.plugin.placeholders(line));
					}
					e.setCancelled(true);
				}
			}
		}
	}

}
