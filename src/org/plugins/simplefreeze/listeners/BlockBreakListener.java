package org.plugins.simplefreeze.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.PlayerManager;

/**
 * Created by Rory on 3/10/2017.
 */
public class BlockBreakListener implements Listener {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;

    public BlockBreakListener(SimpleFreezeMain plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (this.playerManager.isFrozen(e.getPlayer()) && !this.plugin.getConfig().getBoolean("block-break")) {
            e.setCancelled(true);
            for (String msg : this.plugin.getConfig().getStringList("block-break-message")) {
                e.getPlayer().sendMessage(this.plugin.placeholders(msg));
            }
        }
    }

}
