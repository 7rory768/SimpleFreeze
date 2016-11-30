package org.plugins.simplefreeze.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.FreezeManager;
import org.plugins.simplefreeze.managers.HelmetManager;
import org.plugins.simplefreeze.managers.PlayerManager;
import org.plugins.simplefreeze.objects.FrozenPlayer;
import org.plugins.simplefreeze.objects.SFLocation;
import org.plugins.simplefreeze.util.DataConverter;
import org.plugins.simplefreeze.util.UpdateNotifier;

public class PlayerJoinListener implements Listener {

    private final SimpleFreezeMain plugin;
    private final FreezeManager freezeManager;
    private final PlayerManager playerManager;
    private final HelmetManager helmetManager;

    public PlayerJoinListener(SimpleFreezeMain plugin, FreezeManager freezeManager, PlayerManager playerManager, HelmetManager helmetManager) {
        this.plugin = plugin;
        this.freezeManager = freezeManager;
        this.playerManager = playerManager;
        this.helmetManager = helmetManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        final String uuidStr = p.getUniqueId().toString();
        if (p.hasPermission("sf.notify.update") && !UpdateNotifier.getLatestVersion().equals(UpdateNotifier.getCurrentVersion())) {
            new BukkitRunnable() {

                @Override
                public void run() {
                    p.sendMessage(plugin.placeholders("{PREFIX}You are still running version &b" + UpdateNotifier.getCurrentVersion() + "\n{PREFIX}Latest version: &b" + UpdateNotifier.getLatestVersion()));

                }

            }.runTaskLater(this.plugin, 25L);
        }
        FrozenPlayer frozenPlayer;
        if (DataConverter.hasDataToConvert(p)) {
            frozenPlayer = DataConverter.convertData(p);
            long freezeDate = System.currentTimeMillis();
            this.plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".freeze-date", freezeDate);
            this.plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".freezee-name", p.getName());
            this.plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".freezer-name", "null");
            this.plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".orginal-location", frozenPlayer.getOriginalLoc());
            this.plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".freeze-location", new SFLocation(p.getLocation()).toString());
            this.plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".mysql", false);
            this.plugin.getPlayerConfig().saveConfig();
            this.plugin.getPlayerConfig().reloadConfig();
            DataConverter.removeData(p);
            this.playerManager.addFrozenPlayer(p.getUniqueId(), frozenPlayer);
        }

        if (this.playerManager.isFrozen(p)) {
            frozenPlayer = this.playerManager.getFrozenPlayer(p);
            frozenPlayer.setHelmet(p.getInventory().getHelmet());
            p.getInventory().setHelmet(this.helmetManager.getPersonalHelmetItem(frozenPlayer));
        }
    }

}
