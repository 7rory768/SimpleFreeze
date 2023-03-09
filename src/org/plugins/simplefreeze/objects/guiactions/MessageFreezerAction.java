package org.plugins.simplefreeze.objects.guiactions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.plugins.simplefreeze.objects.players.FrozenPlayer;
import rorys.library.util.MessagingUtil;

public class MessageFreezerAction implements GUIAction {

    private final String message;

    public MessageFreezerAction( String message) {
        this.message = MessagingUtil.format(message);
    }

    @Override
    public void performAction(FrozenPlayer frozenPlayer) {
        Player freezer = Bukkit.getPlayer(frozenPlayer.getFreezerUUID());
        if (freezer != null && freezer.isOnline()) {
            freezer.sendMessage(this.message.replace("{FREEZER}", frozenPlayer.getFreezerName()).replace("{PLAYER}", frozenPlayer.getFreezeeName()));
        }
    }
}
