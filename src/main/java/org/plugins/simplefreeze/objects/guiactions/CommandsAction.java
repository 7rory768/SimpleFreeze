package org.plugins.simplefreeze.objects.guiactions;

import org.bukkit.Bukkit;
import org.plugins.simplefreeze.objects.players.FrozenPlayer;

import java.util.List;

public class CommandsAction implements GUIAction {

    private final List<String> commands;

    public CommandsAction(List<String> commands) {
        this.commands = commands;
    }

    public void performAction(FrozenPlayer frozenPlayer) {
        for (String command : this.commands) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("{PLAYER}", frozenPlayer.getFreezeeName()).replace("{FREEZER}", frozenPlayer.getFreezerName()));
        }
    }

}
