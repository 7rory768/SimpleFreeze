package org.plugins.simplefreeze.hooks;

import me.confuser.banmanager.bukkit.api.events.IpBanEvent;
import me.confuser.banmanager.common.api.BmAPI;
import me.confuser.banmanager.bukkit.api.events.IpBanEvent;
import me.confuser.banmanager.bukkit.api.events.NameBannedEvent;
import me.confuser.banmanager.bukkit.api.events.PlayerBannedEvent;
import me.confuser.banmanager.common.ipaddr.IPAddress;
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
                IPAddress ip = e.getBan().getIp();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    try {
                        if (this.playerManager.isFrozen(p) && !this.playerManager.isFreezeAllFrozen(p) && BmAPI.getPlayer(p.getUniqueId()).getIp().equals(ip)) {
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
