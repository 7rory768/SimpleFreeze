package org.plugins.simplefreeze.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.cache.FrozenPages;

public class FrozenCommand implements CommandExecutor {

    private final SimpleFreezeMain plugin;
    private final FrozenPages frozenPages;

    public FrozenCommand(SimpleFreezeMain plugin, FrozenPages frozenPages) {
        this.plugin = plugin;
        this.frozenPages = frozenPages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("frozen")) {
            if (!sender.hasPermission("sf.frozen")) {
                for (String msg : this.plugin.getConfig().getStringList("no-permission-message")) {
                    if (!msg.equals("")) {
                        sender.sendMessage(this.plugin.placeholders(msg));
                    }
                }
                return false;
            }

            int page = 1;
            if (args.length > 0) {
                if (!this.isInt(args[0])) {
                    sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("not-an-int").replace("{INTEGER}", args[0])));
                    return false;
                }
                page = Integer.parseInt(args[0]);
            }

            if (this.frozenPages.noPages()) {
                sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("nobody-frozen")));
                return false;
            }

            if (page > this.frozenPages.getMaxPageNum()) {
                sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("page-doesnt-exist").replace("{PAGENUM}", "" + page).replace("{MAXPAGENUM}", "" + this.frozenPages.getMaxPageNum())));
                return false;
            }


            sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("frozen-list-format" + ".header").replace("{PAGENUM}", "" + page).replace("{MAXPAGENUM}", "" + this.frozenPages.getMaxPageNum())));
            sender.sendMessage(this.frozenPages.getPage(page));
            sender.sendMessage(this.plugin.placeholders(this.plugin.getConfig().getString("frozen-list-format" + ".footer")));
            return true;
        }

        return false;
    }

    private boolean isInt(String arg) {
        try {
            Integer.parseInt(arg);
        }
        catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}
