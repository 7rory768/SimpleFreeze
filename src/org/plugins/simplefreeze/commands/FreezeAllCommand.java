package org.plugins.simplefreeze.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.FreezeManager;
import org.plugins.simplefreeze.managers.LocationManager;

import java.util.UUID;

public class FreezeAllCommand implements CommandExecutor {

    private final SimpleFreezeMain plugin;
    private final FreezeManager freezeManager;
    private final LocationManager locationManager;

    public FreezeAllCommand(SimpleFreezeMain plugin, FreezeManager freezeManager, LocationManager locationManager) {
        this.plugin = plugin;
        this.freezeManager = freezeManager;
        this.locationManager = locationManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("freezeall")) {
            if (!sender.hasPermission("sf.freezeall")) {
                for (String msg : this.plugin.getConfig().getStringList("no-permission-message")) {
                    if (!msg.equals("")) {
                        sender.sendMessage(this.plugin.placeholders(msg));
                    }
                }
                return false;
            }

            UUID senderUUID = sender instanceof Player ? ((Player) sender).getUniqueId() : null;

            if (this.freezeManager.freezeAllActive()) {
                this.freezeManager.unfreezeAll();
                this.freezeManager.notifyOfUnfreezeAll(senderUUID);
            } else {
                String reason = null;
                String location = null;
                if (args.length > 0) {
                    if (this.plugin.getConfig().isSet("locations." + args[0])) {
                        location = args[0];
                        if (args.length > 1) {
                            reason = "";
                            for (int i = 1; i < args.length; i++) {
                                reason += args[i] + " ";
                            }
                            reason = reason.substring(0, reason.length() - 1);
                        }
                    } else {
                        reason = "";
                        for (int i = 0; i < args.length; i++) {
                            reason += args[i] + " ";
                        }
                        reason = reason.substring(0, reason.length() - 1);
                    }
                }
                if (reason == null) {
                    reason = this.plugin.getConfig().getString("default-reason");
                }
                this.freezeManager.freezeAll(senderUUID, location, reason);
                this.freezeManager.notifyOfFreezeAll(senderUUID, location);
            }

        }

        return false;

    }

}
