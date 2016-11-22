package org.plugins.simplefreeze.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.PlayerManager;

public class EntityDamageListener implements Listener {

	private final SimpleFreezeMain plugin;
	private final PlayerManager playerManager;
	
	public EntityDamageListener(SimpleFreezeMain plugin, PlayerManager playerManager) {
		this.plugin = plugin;
		this.playerManager = playerManager;
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (this.plugin.getConfig().getBoolean("player-damage") && this.playerManager.isFrozen(e.getEntity().getUniqueId())) {
			e.setCancelled(true);
		}
	}

}
