package org.plugins.simplefreeze.hooks;

import me.confuser.banmanager.BmAPI;
import me.confuser.banmanager.events.IpBanEvent;
import me.confuser.banmanager.events.NameBannedEvent;
import me.confuser.banmanager.events.PlayerBannedEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.FreezeManager;
import org.plugins.simplefreeze.managers.PlayerManager;

import java.sql.SQLException;
import java.util.UUID;

public class BanManagerHook implements Listener {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;
    private final FreezeManager freezeManager;

    public BanManagerHook(SimpleFreezeMain plugin, PlayerManager playerManager, FreezeManager freezeManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.freezeManager = freezeManager;
    }

    @EventHandler
    public void onPlayerBanned(PlayerBannedEvent e) {
        if (this.plugin.usingBanManager()) {
            UUID uuid = e.getBan().getPlayer().getUUID();
            if (this.playerManager.isFrozen(uuid) && !this.playerManager.isFreezeAllFrozen(uuid)) {
                this.freezeManager.unfreeze(uuid);
            }
        }
    }

    @EventHandler
    public void onNameBanned(NameBannedEvent e) {
        if (this.plugin.usingBanManager()) {
            UUID uuid = Bukkit.getOfflinePlayer(e.getBan().getName()).getUniqueId();
            if (this.playerManager.isFrozen(uuid) && !this.playerManager.isFreezeAllFrozen(uuid)) {
                this.freezeManager.unfreeze(uuid);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onIPBan(IpBanEvent e) {
        if (this.plugin.usingBanManager()) {
            if (!e.isCancelled()) {
                long ip = e.getBan().getIp();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    try {
                        if (this.playerManager.isFrozen(p) && !this.playerManager.isFreezeAllFrozen(p) && BmAPI.getPlayer(p).getIp() == ip) {
                            this.freezeManager.unfreeze(p.getUniqueId());
                        }
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }
}
