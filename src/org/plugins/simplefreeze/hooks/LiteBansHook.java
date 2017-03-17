package org.plugins.simplefreeze.hooks;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.FreezeManager;
import org.plugins.simplefreeze.managers.PlayerManager;

import java.io.File;

public class LiteBansHook implements Listener {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;
    private final FreezeManager freezeManager;

    private YamlConfiguration messagesConfig;
    private File messagesFile;

    public LiteBansHook(SimpleFreezeMain plugin, PlayerManager playerManager, FreezeManager freezeManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.freezeManager = freezeManager;
    }

    public void setupMessagesConfig() {
        if (this.messagesFile == null) {
            this.messagesFile = new File(new File("plugins" + File.separator + "LiteBans" + File.separator), "messages.yml");
            this.messagesConfig = YamlConfiguration.loadConfiguration(this.messagesFile);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerKickEvent e) {
        if (this.plugin.usingLiteBans()) {
            if (e.getReason() != null) {
                if (e.getReason() != null) {
                    this.setupMessagesConfig();
                    if (!e.getReason().equals("")) {
                        String reasonMsg = "";
                        for (String line : e.getReason().split("\\n+")) {
                            reasonMsg += line + "\n";
                        }

                        if (reasonMsg.length() > 0) {
                            reasonMsg = reasonMsg.substring(0, reasonMsg.length() - 2);
                        }

                        boolean liteBan = false;
                        String[] reasonLines = reasonMsg.replace("§", "&").split("\\n+");
                        String[] bannedLines = this.messagesConfig.getString("banned_message").replace("$base", this.messagesConfig.getString("banned_message_base")).replace("$appealMessage", this.messagesConfig.getString("banned_message_appeal_message")).split("\\n+");
                        String[] bannedPermanentLines = this.messagesConfig.getString("banned_message_permanent").replace("$base", this.messagesConfig.getString("banned_message_base")).replace("$appealMessage", this.messagesConfig.getString("banned_message_appeal_message")).split("\\n+");
                        if (reasonLines.length == bannedLines.length || reasonLines.length == bannedPermanentLines.length) {
                            liteBan = true;
                            for (int i = 0; i < reasonLines.length && i < bannedLines.length && i < bannedPermanentLines.length; i++) {
                                if (!reasonLines[i].equals("")) {
                                    int permanentIndex = bannedPermanentLines[i].contains("$") ? bannedPermanentLines[i].indexOf("$") : reasonLines[i].length();
                                    int bannedIndex = bannedLines[i].contains("$") ? bannedLines[i].indexOf("$") : reasonLines[i].length();
                                    if (!(reasonLines[i].substring(0, permanentIndex).equals(bannedPermanentLines[i].substring(0, permanentIndex)) || (reasonLines[i].substring(0, bannedIndex).equals(bannedLines[i].substring(0, bannedIndex))))) {
                                        liteBan = false;
                                        break;
                                    }
                                }
                            }
                        }

                        if (liteBan && this.playerManager.isFrozen(e.getPlayer()) && !this.playerManager.isFreezeAllFrozen(e.getPlayer().getUniqueId())) {
                            this.freezeManager.unfreeze(e.getPlayer().getUniqueId());
                        }
                    }
                }
            }
        }
    }
}
