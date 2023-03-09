package org.plugins.simplefreeze.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.GUIManager;
import org.plugins.simplefreeze.managers.LocationManager;
import org.plugins.simplefreeze.managers.MessageManager;
import org.plugins.simplefreeze.managers.PlayerManager;
import org.plugins.simplefreeze.objects.FrozenPlayer;
import org.plugins.simplefreeze.objects.TempFrozenPlayer;
import org.plugins.simplefreeze.util.FrozenType;
import org.plugins.simplefreeze.util.TimeUtil;

public class PlayerQuitListener implements Listener {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;
    private final MessageManager messageManager;
    private final LocationManager locationManager;
    private final GUIManager guiManager;

    public PlayerQuitListener(SimpleFreezeMain plugin, PlayerManager playerManager, MessageManager messageManager, LocationManager locationManager, GUIManager guiManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.messageManager = messageManager;
        this.locationManager = locationManager;
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        String uuidStr = p.getUniqueId().toString();
        if (this.playerManager.isFrozen(p)) {

            Location originalLocation = this.playerManager.getOriginalLocation(p.getUniqueId());
            if (originalLocation != null && this.plugin.getConfig().getBoolean("tp-back")) {
                p.teleport(originalLocation);
            }
            p.getInventory().setHelmet(this.playerManager.getFrozenPlayer(p).getHelmet());
            if (this.plugin.getConfig().getBoolean("enable-fly")) {
                p.setAllowFlight(false);
                p.setFlying(false);
            }
            FrozenPlayer frozenPlayer = this.playerManager.getFrozenPlayer(p);
            FrozenType type = frozenPlayer.getType();

            String time = "Permanent";
            if (type == FrozenType.TEMP_FROZEN) {
                TempFrozenPlayer tempFrozenPlayer = (TempFrozenPlayer) frozenPlayer;
                tempFrozenPlayer.cancelTask();
                time = TimeUtil.formatTime((tempFrozenPlayer.getUnfreezeDate() - System.currentTimeMillis())/1000L);
                if (!this.plugin.getConfig().getBoolean("count-time-offline")) {
                    this.plugin.getPlayerConfig().getConfig().set("players." + uuidStr + ".last-online-time", System.currentTimeMillis());
                    this.plugin.getPlayerConfig().saveConfig();
                    this.plugin.getPlayerConfig().reloadConfig();
                }
            }

            if (this.guiManager.isGUIEnabled() && (type != FrozenType.FREEZEALL_FROZEN || (this.guiManager.isFreezeAllGUIEnabled() && type == FrozenType.FREEZEALL_FROZEN))) {
                this.guiManager.removePlayer(p.getUniqueId());
            }

            String player = p.getName();
            String freezer = frozenPlayer.getFreezerName();
            String locationPlaceholder = this.locationManager.getLocationPlaceholder(this.locationManager.getLocationName(frozenPlayer.getFreezeLoc()));
            String reason = frozenPlayer.getReason();
            String servers = this.plugin.getConfig().getString("players." + p.getUniqueId().toString() + ".servers", this.plugin.getServerID());

            if (!(this.playerManager.isFreezeAllFrozen(p) && !this.plugin.getConfig().getBoolean("leave-message-during-freezeall"))) {
                for (Player onlineP : Bukkit.getServer().getOnlinePlayers()) {
                    if (onlineP.hasPermission("sf.notify.leave")) {
                        for (String msg : this.plugin.getConfig().getStringList("notify-on-leave-message")) {
                            onlineP.sendMessage(this.plugin.placeholders(msg.replace("{PLAYER}", player).replace("{FREEZER}", freezer).replace("{LOCATION}", locationPlaceholder).replace("{TIME}", time).replace("{REASON}", reason).replace("{SERVERS}", servers)));
                        }
                    }
                }
            }

            String commandsPath = "";
            if (type == FrozenType.FROZEN) {
                commandsPath = "logout-commands.freeze";
            } else if (type == FrozenType.TEMP_FROZEN) {
                commandsPath = "logout-commands.tempfreeze";
            } else {
                commandsPath = "logout-commands.freezeall";
            }
            for (String cmd : this.plugin.getConfig().getStringList(commandsPath)) {
                if (cmd.startsWith("/")) {
                    cmd = cmd.substring(1);
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{PLAYER}", player).replace("{FREEZER}", freezer).replace("{LOCATION}", locationPlaceholder).replace("{TIME}", time).replace("{REASON}", reason).replace("{SERVERS}", servers));
            }

            this.playerManager.removeFrozenPlayer(p);
            this.messageManager.removePlayer(p);
        }
    }

}
