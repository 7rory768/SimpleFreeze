package org.plugins.simplefreeze.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.FreezeManager;
import org.plugins.simplefreeze.managers.LocationManager;
import org.plugins.simplefreeze.managers.PlayerManager;
import org.plugins.simplefreeze.objects.FrozenPlayer;
import org.plugins.simplefreeze.objects.TempFrozenPlayer;
import org.plugins.simplefreeze.util.TimeUtil;

public class FrozenCommand implements CommandExecutor {

    private final SimpleFreezeMain plugin;
    private final FreezeManager freezeManager;
    private final PlayerManager playerManager;
    private final LocationManager locationManager;

    public FrozenCommand(SimpleFreezeMain plugin, FreezeManager freezeManager, PlayerManager playerManager, LocationManager locationManager) {
        this.plugin = plugin;
        this.freezeManager = freezeManager;
        this.playerManager = playerManager;
        this.locationManager = locationManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("frozen")) {
            if (!sender.hasPermission("sf.frozen")) {
                sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("no-permission-message")));
                return false;
            }

            sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("frozen-list-format.header")));

            if (this.playerManager.getFrozenPlayers().isEmpty()) {
                sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("nobody-frozen")));
            }

            for (FrozenPlayer frozenPlayer : this.playerManager.getFrozenPlayers().values()) {
                String path = "frozen";
                String onlinePlaceholder = Bukkit.getPlayerExact(frozenPlayer.getFreezeeName()) != null ? this.plugin.getConfig().getString("frozen-list-format.online-placeholder") : this.plugin.getConfig().getString("frozen-list-format.offline-placeholder");
                String playerPlaceholder = frozenPlayer.getFreezeeName();
                String freezerPlaceholder = frozenPlayer.getFreezerName();
                String timePlaceholder = frozenPlayer instanceof TempFrozenPlayer ? TimeUtil.formatTime((((TempFrozenPlayer) frozenPlayer).getUnfreezeDate() - System.currentTimeMillis()) / 1000L) : "";
                if (!timePlaceholder.equals("")) {
                    path = "temp-frozen";
                }
                String locationPlaceholder = this.locationManager.getLocationName(frozenPlayer.getFreezeLoc());
                if (!locationPlaceholder.equals("Unknown")) {
                    path += "-location";
                }

                sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("frozen-list-format.formats." + path).replace("{ONLINE}", onlinePlaceholder).replace("{PLAYER}",
                        playerPlaceholder).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder).replace("{FREEZER}", freezerPlaceholder)));
            }

            sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("frozen-list-format.footer")));
            return true;
        }

        return false;
    }

}
