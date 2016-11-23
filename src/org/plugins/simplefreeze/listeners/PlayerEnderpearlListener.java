package org.plugins.simplefreeze.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.PlayerManager;

public class PlayerEnderpearlListener implements Listener {

	private final SimpleFreezeMain plugin;
	private final PlayerManager playerManager;
	
	public PlayerEnderpearlListener(SimpleFreezeMain plugin, PlayerManager playerManager) {
		this.plugin = plugin;
		this.playerManager = playerManager;
	}
	
	@EventHandler
	public void onEnderpearl(PlayerTeleportEvent e) {
		if (e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL && this.playerManager.isFrozen(e.getPlayer())) {
			e.setCancelled(true);
			e.getPlayer().getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
			for (String msg : this.plugin.getConfig().getStringList("enderpearl-message")) {
				e.getPlayer().sendMessage(this.plugin.placeholders(msg));
			}
		}
		
	}

}
