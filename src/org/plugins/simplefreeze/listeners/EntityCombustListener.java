package org.plugins.simplefreeze.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.PlayerManager;

public class EntityCombustListener implements Listener {

	private final SimpleFreezeMain plugin;
	private final PlayerManager playerManager;
	
	public EntityCombustListener(SimpleFreezeMain plugin, PlayerManager playerManager) {
		this.plugin = plugin;
		this.playerManager = playerManager;
	}
	
	@EventHandler
	public void onEntityCombust(EntityCombustEvent e) {
		if (e.getEntity() instanceof Player) {
			if (this.playerManager.isFrozen(e.getEntity().getUniqueId())) {
				if (this.plugin.getConfig().getBoolean("fire-damage")) {
					e.setCancelled(true);
				}
			}
		}
	}
	
}
