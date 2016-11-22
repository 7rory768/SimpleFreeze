package org.plugins.simplefreeze.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
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
		
	}

}
