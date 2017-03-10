package org.plugins.simplefreeze.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.PlayerManager;

/**
 * Created by Rory on 3/9/2017.
 */
public class PlayerEditBookListener implements Listener {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;

    public PlayerEditBookListener(SimpleFreezeMain plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onBookEdit(PlayerEditBookEvent e) {
        if (this.playerManager.isFrozen(e.getPlayer()) && this.plugin.getConfig().getBoolean("book-editing")) {
            e.setCancelled(true);
            for (String msg : this.plugin.getConfig().getStringList("book-edit-message")) {
                e.getPlayer().sendMessage(this.plugin.placeholders(msg));
            }
        }
    }

}
