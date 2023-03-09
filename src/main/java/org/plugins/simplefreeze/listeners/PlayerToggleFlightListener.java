package org.plugins.simplefreeze.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.PlayerManager;

/**
 * Created by Rory on 3/2/2017.
 */
public class PlayerToggleFlightListener implements Listener {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;

    public PlayerToggleFlightListener(SimpleFreezeMain plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onToggleFly(PlayerToggleFlightEvent e) {
        if (this.playerManager.isFrozen(e.getPlayer()) && e.getPlayer().isFlying()) {
            e.setCancelled(true);
        }
    }

}
