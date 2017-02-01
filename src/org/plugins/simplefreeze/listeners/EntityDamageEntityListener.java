package org.plugins.simplefreeze.listeners;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.PlayerManager;

public class EntityDamageEntityListener implements Listener {

	private final SimpleFreezeMain plugin;
	private final PlayerManager playerManager;

	public EntityDamageEntityListener(SimpleFreezeMain plugin, PlayerManager playerManager) {
		this.plugin = plugin;
		this.playerManager = playerManager;
	}

	@EventHandler
	public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
		if (this.plugin.getConfig().getBoolean("player-damage")) {
			if (e.getDamager() instanceof Player) {
				Player p = (Player) e.getDamager();
				if (this.playerManager.isFrozen(p)) {
					e.setCancelled(true);
					for (String msg : this.plugin.getConfig().getStringList("frozen-attack-message")) {
						if (!msg.equals("")) {
							p.sendMessage(this.plugin.placeholders(msg));
						}
					}
				} else if (this.playerManager.isFrozen(e.getEntity().getUniqueId())) {
					e.setCancelled(true);
					for (String msg : this.plugin.getConfig().getStringList("frozen-attacked-message")) {
						if (!msg.equals("")) {
							p.sendMessage(this.plugin.placeholders(msg.replace("{PLAYER}", ((Player) e.getEntity()).getName())));
						}
					}
				}
			} else if (e.getEntity() instanceof Player && e.getDamager() instanceof Projectile) {
				Projectile projectile = (Projectile) e.getDamager();
				ProjectileSource projectileSource = projectile.getShooter();
				if (projectileSource != null) {
					if (projectileSource instanceof Player) {
						Player p = (Player) projectileSource;
						if (this.playerManager.isFrozen(p)) {
							e.setCancelled(true);
							for (String msg : this.plugin.getConfig().getStringList("frozen-attack-message")) {
								if (!msg.equals("")) {
									p.sendMessage(this.plugin.placeholders(msg));
								}
							}
						}
					}
				}
			}
		}
	}

}
