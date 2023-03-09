package org.plugins.simplefreeze.listeners;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.PlayerManager;

/**
 * Created by Rory on 3/9/2017.
 */
public class ProjectileLaunchListener implements Listener {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;

    public ProjectileLaunchListener(SimpleFreezeMain plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onLaunchEvent(ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player) {
            Player p = (Player) e.getEntity().getShooter();
            if (this.playerManager.isFrozen(p)) {
                EntityType type = e.getEntityType();
                if (type == EntityType.FISHING_HOOK) {
                    e.setCancelled(true);
                    for (String msg : this.plugin.getConfig().getStringList("fishing-rod-message")) {
                        p.sendMessage(this.plugin.placeholders(msg));
                    }
                } else if (type == EntityType.ARROW) {
                    e.setCancelled(true);
                    for (String msg : this.plugin.getConfig().getStringList("bow-shoot-message")) {
                        p.sendMessage(this.plugin.placeholders(msg));
                        p.getInventory().addItem(new ItemStack(Material.ARROW));
                    }
                } else if (type == EntityType.SPLASH_POTION) {
                    e.setCancelled(true);
                    for (String msg : this.plugin.getConfig().getStringList("splash-potion-message")) {
                        p.sendMessage(this.plugin.placeholders(msg));
                    }
                } else if (type == EntityType.SNOWBALL) {
                    e.setCancelled(true);
                    for (String msg : this.plugin.getConfig().getStringList("snowball-message")) {
                        p.sendMessage(this.plugin.placeholders(msg));
                        p.getInventory().addItem(new ItemStack(Material.SNOW_BALL));
                    }
                } else if (type == EntityType.EGG) {
                    e.setCancelled(true);
                    for (String msg : this.plugin.getConfig().getStringList("egg-message")) {
                        p.sendMessage(this.plugin.placeholders(msg));
                        p.getInventory().addItem(new ItemStack(Material.EGG));
                    }
                } else if (type == EntityType.THROWN_EXP_BOTTLE) {
                    e.setCancelled(true);
                    for (String msg : this.plugin.getConfig().getStringList("exp-bottle-message")) {
                        p.sendMessage(this.plugin.placeholders(msg));
                        p.getInventory().addItem(new ItemStack(Material.EXP_BOTTLE));
                    }
                }

            }
        }
    }

}
