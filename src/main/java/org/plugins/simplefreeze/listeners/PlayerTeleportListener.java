package org.plugins.simplefreeze.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.PlayerManager;

public class PlayerTeleportListener implements Listener {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;

    public PlayerTeleportListener(SimpleFreezeMain plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL && this.playerManager.isFrozen(e.getPlayer())) {
            e.setCancelled(true);
            e.getPlayer().getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
            for (String msg : this.plugin.getConfig().getStringList("enderpearl-message")) {
                e.getPlayer().sendMessage(this.plugin.placeholders(msg));
            }
        }

        if ((e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL || e.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) && this.playerManager.isFrozen(e.getPlayer())) {
            e.setCancelled(true);
        }

    }

}
