package org.plugins.simplefreeze.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.PlayerManager;

public class PlayerDropListener implements Listener {

	private final SimpleFreezeMain plugin;
	private final PlayerManager playerManager;
	
	public PlayerDropListener(SimpleFreezeMain plugin, PlayerManager playerManager) {
		this.plugin = plugin;
		this.playerManager = playerManager;
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent e) {
		if (this.playerManager.isFrozen(e.getPlayer()) && !this.plugin.getConfig().getBoolean("item-drop")) {
		    e.setCancelled(true);
		    for (String msg : this.plugin.getConfig().getStringList("item-drop-message")) {
		        e.getPlayer().sendMessage(this.plugin.placeholders(msg));
            }
		}
	}

}
