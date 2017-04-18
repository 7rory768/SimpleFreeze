package org.plugins.simplefreeze.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.PlayerManager;

/**
 * Created by Rory on 4/17/2017.
 */
public class BlockPlaceListener implements Listener {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;

    public BlockPlaceListener(SimpleFreezeMain plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (this.playerManager.isFrozen(e.getPlayer()) && !this.plugin.getConfig().getBoolean("block-place")) {
            e.setCancelled(true);
            for (String msg : this.plugin.getConfig().getStringList("block-place-message")) {
                e.getPlayer().sendMessage(this.plugin.placeholders(msg));
            }
        }
    }
}
