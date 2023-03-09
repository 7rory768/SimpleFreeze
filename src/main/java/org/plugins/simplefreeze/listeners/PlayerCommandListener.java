package org.plugins.simplefreeze.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.PlayerManager;

public class PlayerCommandListener implements Listener {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;

    public PlayerCommandListener(SimpleFreezeMain plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }

    @EventHandler
    public void preCommand(PlayerCommandPreprocessEvent e) {
        if (this.playerManager.isFrozen(e.getPlayer())) {
            String message = e.getMessage().toLowerCase() + " ";
            boolean match = false;
            String cmdPlaceholder = "";
            for (String cmd : this.plugin.getConfig().getStringList("blocked-commands")) {
                if (message.startsWith(cmd.toLowerCase() + " ")) {
                    match = true;
                    cmdPlaceholder = cmd;
                    break;
                }
            }

            if (this.plugin.getConfig().getBoolean("whitelist-or-blacklist") && !match) {
                for (String msg : this.plugin.getConfig().getStringList("blocked-command-message")) {
                    cmdPlaceholder = message.substring(0, message.length() - 1);
                    e.getPlayer().sendMessage(this.plugin.placeholders(msg.replace("{COMMAND}", cmdPlaceholder)));
                }
                e.setCancelled(true);
            } else if (!this.plugin.getConfig().getBoolean("whitelist-or-blacklist") && match) {
                for (String msg : this.plugin.getConfig().getStringList("blocked-command-message")) {
                    e.getPlayer().sendMessage(this.plugin.placeholders(msg.replace("{COMMAND}", cmdPlaceholder)));
                }
                e.setCancelled(true);
            }
        }
    }

}
