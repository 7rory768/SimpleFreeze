package org.plugins.simplefreeze.util;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Rory on 6/22/2017.
 */
public class MessagingUtil {

    private final JavaPlugin plugin;
    private String prefix = "";
    private String finalPrefixFormatting = "";

    public MessagingUtil(JavaPlugin plugin) {
        this.plugin = plugin;
        this.updatePrefix();
    }

    public void updatePrefix() {
        this.prefix = this.plugin.getConfig().getString("prefix");
        this.updatePrefixFormatting();
    }

    private void updatePrefixFormatting() {
        String color = "", format = "";
        if (this.prefix.length() > 1) {
            for (int index = this.prefix.length(); index > 1; index--) {
                String bit = this.prefix.substring(index - 2, index);
                if (bit.startsWith("§")) {
                    int chNum = (int) bit.toLowerCase().charAt(1);
                    if ((97 <= chNum && chNum <= 102) || (48 <= chNum && chNum <= 57) || chNum == 114) {
                        color = bit;
                        break;
                    }
                    if (107 <= chNum && chNum <= 112) {
                        format += bit;
                    }

                }
            }
        }
        this.finalPrefixFormatting = color + format;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getPrefixFormatting() {
        return this.finalPrefixFormatting;
    }

    public void sendMessage(CommandSender sender, String msg) {
        sender.sendMessage(this.placeholders(msg));
    }

    public void sendMessageAtPath(CommandSender sender, String path) {
        sender.sendMessage(this.placeholders(this.plugin.getConfig().getString(path)));
    }

    public void broadcastMessage(String msg) {
        Bukkit.broadcastMessage(this.placeholders(msg));
    }

    public void broadcastMessageAtPath(String path) {
        Bukkit.broadcastMessage(this.placeholders(this.plugin.getConfig().getString(path)));
    }

    public String placeholders(String arg) {
        return StringEscapeUtils.unescapeJava(ChatColor.translateAlternateColorCodes('&', arg.replace("{PREFIX}", this.prefix)));
    }

    public static String format(String arg) {
        return StringEscapeUtils.unescapeJava(ChatColor.translateAlternateColorCodes('&', arg));
    }
}
