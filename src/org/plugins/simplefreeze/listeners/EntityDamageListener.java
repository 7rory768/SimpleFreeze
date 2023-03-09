package org.plugins.simplefreeze.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.PlayerManager;

import java.util.UUID;

public class EntityDamageListener implements Listener {

	private final SimpleFreezeMain plugin;
	private final PlayerManager playerManager;
	
	public EntityDamageListener(SimpleFreezeMain plugin, PlayerManager playerManager) {
		this.plugin = plugin;
		this.playerManager = playerManager;
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		UUID uuid = e.getEntity().getUniqueId();
		if (!this.plugin.getConfig().getBoolean("player-damage") && this.playerManager.isFrozen(uuid)) {
			e.setCancelled(true);
			return;
		}

		if (this.playerManager.isFallingPlayer(uuid) && e.getCause() == EntityDamageEvent.DamageCause.FALL) {
			e.setCancelled(true);
			this.playerManager.removeFallingPlayer(uuid);
		}
	}

}
