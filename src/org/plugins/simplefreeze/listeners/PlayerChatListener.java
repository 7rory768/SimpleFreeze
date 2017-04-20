package org.plugins.simplefreeze.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.PlayerManager;

public class PlayerChatListener implements Listener {

	private final SimpleFreezeMain plugin;
	private final PlayerManager playerManager;
	
	public PlayerChatListener(SimpleFreezeMain plugin, PlayerManager playerManager) {
		this.plugin = plugin;
		this.playerManager = playerManager;
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		if (this.playerManager.isFrozen(e.getPlayer()) && this.plugin.getConfig().getBoolean("block-chat")) {
			e.setCancelled(true);
			for (String msg : this.plugin.getConfig().getStringList("block-chat-message")) {
				e.getPlayer().sendMessage(this.plugin.placeholders(msg));
			}
		}
	}

}
