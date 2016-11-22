package org.plugins.simplefreeze.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.PlayerManager;

public class PlayerQuitListener implements Listener {

	private final SimpleFreezeMain plugin;
	private final PlayerManager playerManager;
	
	public PlayerQuitListener(SimpleFreezeMain plugin, PlayerManager playerManager) {
		this.plugin = plugin;
		this.playerManager = playerManager;
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		
	}

}
