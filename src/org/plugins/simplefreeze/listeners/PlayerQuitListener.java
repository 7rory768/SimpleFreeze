package org.plugins.simplefreeze.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.PlayerManager;
import org.plugins.simplefreeze.objects.TempFrozenPlayer;

public class PlayerQuitListener implements Listener {

	private final SimpleFreezeMain plugin;
	private final PlayerManager playerManager;
	
	public PlayerQuitListener(SimpleFreezeMain plugin, PlayerManager playerManager) {
		this.plugin = plugin;
		this.playerManager = playerManager;
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if (this.playerManager.isFrozen(e.getPlayer())) {
			e.getPlayer().getInventory().setHelmet(this.playerManager.getFrozenPlayer(e.getPlayer()).getHelmet());
			if (this.plugin.getConfig().getBoolean("enable-fly")) {
				e.getPlayer().setAllowFlight(false);
				e.getPlayer().setFlying(false);
			}
			if (this.playerManager.getFrozenPlayer(e.getPlayer()) instanceof TempFrozenPlayer) {
				((TempFrozenPlayer) this.playerManager.getFrozenPlayer(e.getPlayer())).cancelTask();
			}
			this.playerManager.removeFrozenPlayer(e.getPlayer());
		}
	}

}
