package org.plugins.simplefreeze;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.plugins.simplefreeze.commands.*;
import org.plugins.simplefreeze.listeners.*;
import org.plugins.simplefreeze.managers.*;
import org.plugins.simplefreeze.objects.FrozenPlayer;
import org.plugins.simplefreeze.util.PlayersConfig;

/* 
 * TODO:
 *  - Remember when freezeall is active after crash/restart
 *  - Add option to spawn block below player if they are in the ground or make it so they cannot be kicked for fly/stop anticheat spam
 *  - Titles
 *  - Actionbar
 *  - On leave give helmet back and on join get rid of it
 *  - Block projectile shooting (ex. bow shooting, eggs, fishing rod, splash potions)
 *  - Particles change on /sf reload
 *  - Make time placeholder on head item update every second
 *  
 *  TODO: Hopefully
 *  - Hologram above players head on freeze option
 *
 * */

/* CHANGES:
 *   - ALLOW OFFLINE FREEZING/UNFREEZING
 *   - PLAYERS MAY NOW EDIT THEIR INVENTORY WHILE FROZEN
 *   - SQL FREEZING
 *   - TEMPORARY FREEZING
 *   - HEAD-BLOCK HAS MORE OPTIONS, CAN BE ANY ITEM/BLOCK, PLACEHOLDERS, ITEMFLAGS, ENCHANTS, LORE, NAME
 *   - HEAD-BLOCK ALSO WILL BE PUT ON A PLAYERS HEAD EVEN IF THEY ALREADY HAVE A HELMET AND THEIR INVENTORY IS FULL
 *   - /SF RELOAD NOW UPDATES HELMETS AND PARTICLES IF THEY ARE CHANGED
 *   - CUSTOMIZABLE /FROZEN FORMAT
 *   - REPLACED THE OLD .TXT FORMAT WITH A NEW CLEANER AND FASTER .YML FORMAT
 *       - ON JOIN DATA WILL CONVERT PER PLAY SO DON'T DELETE UNTIL/UNLESS THE FILE IS EMPTY
 * 
 * BUGS:
 *   - FIXED BUG WHERE PLAYERS WERE SOMETIMES TELEPORTED INTO SUFFICATION THROUGH THE TELEPORT-UP OPTION
 * 
 * */

/* NMS:
 * net.minecraft.server.v1_7_R4
 * net.minecraft.server.v1_8_R1
 * net.minecraft.server.v1_8_R2
 * net.minecraft.server.v1_8_R3
 * net.minecraft.server.v1_9_R1
 * net.minecraft.server.v1_9_R2
 * net.minecraft.server.v1_10_R1
 * */

public class SimpleFreezeMain extends JavaPlugin {

    private String finalPrefixFormatting = this.updateFinalPrefixFormatting();

    private final PlayerManager playerManager = new PlayerManager();

    private FreezeManager freezeManager;
    private PlayersConfig playersConfig;
    private HelmetManager helmetManager;
    private LocationManager locationManager;
    private SQLManager sqlManager;

    @Override
    public void onEnable() {
        this.initializeVariables();
        this.loadConfigs();
        this.registerCommands();
        this.registerListeners();
        // LOAD DATA, AND IF PLAYERS ARE ONLINE, FREEZE THEM
    }

    @Override
    public void onDisable() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (this.playerManager.isFrozen(p)) {
                FrozenPlayer frozenPlayer = this.playerManager.getFrozenPlayer(p);
                p.teleport(frozenPlayer.getOriginalLoc());
                p.getInventory().setHelmet(frozenPlayer.getHelmet());
                p.sendMessage(this.placeholders("{PREFIX}SimpleFreeze has been disabled, you will remain unfrozen until it is re-enabled"));
            }
        }
    }

    private void initializeVariables() {
        this.playersConfig = new PlayersConfig(this);
        this.sqlManager = new SQLManager(this);
        this.locationManager = new LocationManager(this);
        this.helmetManager = new HelmetManager(this, this.playerManager, this.locationManager);
        this.freezeManager = new FreezeManager(this, this.playerManager, this.helmetManager, this.locationManager, this.sqlManager);
    }

    private void loadConfigs() {
        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();
        this.reloadConfig();

        this.playersConfig.getConfig().options().copyDefaults(true);
        this.playersConfig.saveDefaultConfig();
        this.playersConfig.reloadConfig();
    }

    private void registerCommands() {
        this.getCommand("simplefreeze").setExecutor(new SimpleFreezeCommand(this, this.freezeManager, this.helmetManager));
        this.getCommand("freeze").setExecutor(new FreezeCommand(this, this.playerManager, this.freezeManager));
        this.getCommand("tempfreeze").setExecutor(new TempFreezeCommand(this, this.playerManager, this.freezeManager));
        this.getCommand("unfreeze").setExecutor(new UnfreezeCommand(this, this.playerManager, this.freezeManager));
        this.getCommand("frozen").setExecutor(new FrozenCommand(this, this.freezeManager, this.playerManager, this.locationManager));
    }

    private void registerListeners() {
        PluginManager plManager = this.getServer().getPluginManager();
        plManager.registerEvents(new EntityCombustListener(this, this.playerManager), this);
        plManager.registerEvents(new EntityDamageEntityListener(this, this.playerManager), this);
        plManager.registerEvents(new EntityDamageListener(this, this.playerManager), this);
        plManager.registerEvents(new InventoryClickListener(this, this.playerManager), this);
        plManager.registerEvents(new PlayerChatListener(this, this.playerManager), this);
        plManager.registerEvents(new PlayerCommandListener(this, this.playerManager), this);
        plManager.registerEvents(new PlayerDropListener(this, this.playerManager), this);
        plManager.registerEvents(new PlayerEnderpearlListener(this, this.playerManager), this);
        plManager.registerEvents(new PlayerInteractListener(this, this.playerManager), this);
        plManager.registerEvents(new PlayerJoinListener(this, this.freezeManager, this.playerManager, this.helmetManager), this);
        plManager.registerEvents(new PlayerMoveListener(this, this.playerManager), this);
        plManager.registerEvents(new PlayerQuitListener(this, this.playerManager), this);
    }

    public PlayersConfig getPlayerConfig() {
        return this.playersConfig;
    }

    public String placeholders(String arg) {
        return StringEscapeUtils.unescapeJava(ChatColor.translateAlternateColorCodes('&', arg.replace("{PREFIX}", this.getConfig().getString("prefix"))));
    }

    public String getHelpMessage() {
        return this.placeholders("                                           &b&lSimpleFreeze\n" + "&b/sf &8- &7Displays this message\n" + "&b/sf reload &8- &7Reloads configuration file\n" + "&b/frozen &8- &7Lists frozen players\n"
                + "&b/freeze <name> [location] [servers] &8- &7Freezes a player\n" + "&b/tempfreeze <name> <time> [servers] &8- &7Temporarily freezes a player\n" + "&b/unfreeze <name> &8- &7Unfreezes a player\n"
                + "&b/freezeall &8- &7Freeze all players\n");
    }

    public String getFinalPrefixFormatting() {
        return this.finalPrefixFormatting;
    }

    public String updateFinalPrefixFormatting() {
        String color = "", format = "";
        String prefix = this.placeholders("{PREFIX}");
        if (prefix.length() > 1) {
            for (int index = prefix.length(); index > 1; index--) {
                String bit = prefix.substring(index - 2, index);
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
        return color + format;
    }

}
