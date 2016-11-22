package org.plugins.simplefreeze.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.PlayerManager;

public class PlayerMoveListener implements Listener {

	private final SimpleFreezeMain plugin;
	private final PlayerManager playerManager;

	public PlayerMoveListener(SimpleFreezeMain plugin, PlayerManager playerManager) {
		this.plugin = plugin;
		this.playerManager = playerManager;
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (this.playerManager.isFrozen(e.getPlayer())) {
			if (this.plugin.getConfig().getBoolean("head-movement")) {
				e.getPlayer().teleport(e.getFrom());
			} else if ((e.getFrom().getX() != e.getTo().getX() || e.getFrom().getZ() != e.getTo().getZ() || e.getFrom().getY() != e.getTo().getY())) {
				e.getPlayer().teleport(e.getFrom());
			}
		}
	}

}
