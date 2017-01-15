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

            UUID senderUUID = sender instanceof Player ? ((Player)sender).getUniqueId() : null;

            if (this.freezeManager.freezeAllActive()) {
                this.freezeManager.unfreezeAll();
                this.freezeManager.notifyOfUnfreezeAll(senderUUID);
            } else if (args.length > 0) {
                if (!this.plugin.getConfig().isSet("locations." + args[0])) {
                    sender.sendMessage(this.plugin.placeholders("{PREFIX}&b" + args[0] + " &7is not a valid location, try:"));
                    String locations = "";
                    for (String locationName : this.plugin.getConfig().getConfigurationSection("locations").getKeys(false)) {
                        locations += "&b" + locationName + this.plugin.getFinalPrefixFormatting() + ", ";
                    }
                    sender.sendMessage(this.plugin.placeholders(locations.substring(0, locations.length() - 2)));
                    return false;
                } else {
                    this.freezeManager.freezeAll(senderUUID, args[0]);
                    this.freezeManager.notifyOfFreezeAll(senderUUID, args[0]);
                }
            } else {
                this.freezeManager.freezeAll(senderUUID, null);
                this.freezeManager.notifyOfFreezeAll(senderUUID, null);
            }

        }

        return false;

    }

}
