package org.plugins.simplefreeze;

import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.plugins.simplefreeze.cache.FrozenPages;
import org.plugins.simplefreeze.commands.*;
import org.plugins.simplefreeze.hooks.BanManagerHook;
import org.plugins.simplefreeze.hooks.EssentialsHook;
import org.plugins.simplefreeze.hooks.LiteBansHook;
import org.plugins.simplefreeze.listeners.*;
import org.plugins.simplefreeze.managers.*;
import org.plugins.simplefreeze.objects.FreezeAllPlayer;
import org.plugins.simplefreeze.objects.FrozenPlayer;
import org.plugins.simplefreeze.objects.SFLocation;
import org.plugins.simplefreeze.objects.TempFrozenPlayer;
import org.plugins.simplefreeze.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/* 
 * TODO
 *  - Player history
 *  - Effects on freeze
 *  - GUI for commands
 *  - Hologram above players head on freeze option
 *  - Titles
 *  - Actionbar
 *
 */

/* CHANGES:
 *
 */

/* NMS:
 * net.minecraft.server.v1_7_R4
 * net.minecraft.server.v1_8_R1
 * net.minecraft.server.v1_8_R2
 * net.minecraft.server.v1_8_R3
 * net.minecraft.server.v1_9_R1
 * net.minecraft.server.v1_9_R2
 * net.minecraft.server.v1_10_R1
 */

public class SimpleFreezeMain extends JavaPlugin {

    private String finalPrefixFormatting = this.updateFinalPrefixFormatting();

    private Permission permission = null;
    private boolean usingLiteBans = false;
    private boolean usingBanManager = false;
    private boolean usingEssentials = false;
    private boolean usingMySQL = false;

    private MySQL mySQL;
    private PlayerManager playerManager;
    private FreezeManager freezeManager;
    private PlayersConfig playersConfig;
    private StatsConfig statsConfig;
    private LocationsConfig locationsConfig;
    private HelmetManager helmetManager;
    private LocationManager locationManager;
    private SQLManager sqlManager;
    private FrozenPages frozenPages;
    private DataConverter dataConverter;
    private ParticleManager particleManager;
    private SoundManager soundManager;
    private MessageManager messageManager;
    private MovementManager movementManager;

    private String serverID;

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            this.permission = permissionProvider.getProvider();
        }
        return (this.permission != null);
    }

    @Override
    public void onEnable() {
        this.initializeVariables();
        this.loadConfigs();
        this.soundManager.reset();
        if (this.vaultEnabled()) {
            this.setupPermissions();
            Bukkit.getConsoleSender().sendMessage(this.placeholders("[SimpleFreeze] Vault found, offline freezing &aenabled"));
        } else {
            Bukkit.getConsoleSender().sendMessage(this.placeholders("[SimpleFreeze] Vault not found, offline freezing &cdisabled"));
        }
        if (this.getConfig().getBoolean("mysql.enabled")) {
            if (this.mySQL.getConnection() == null) {
                this.getServer().getConsoleSender().sendMessage(this.placeholders("[SimpleFreeze] Unable to connect to MySQL database, sqlfreeze &cdisabled"));
            } else {
                this.getServer().getConsoleSender().sendMessage(this.placeholders("[SimpleFreeze] Successfully connected to MySQL database, &asqlfreeze enabled"));
                this.usingMySQL = true;
                this.sqlManager.setupTables();
                this.sqlManager.setupTasks();
                List<UUID> frozenList = new ArrayList<UUID>();
                for (String uuidStr : this.getPlayerConfig().getConfig().getConfigurationSection("players").getKeys(false)) {
                    frozenList.add(UUID.fromString(uuidStr));
                }
                this.sqlManager.syncFrozenList(frozenList);
            }
        }
        this.registerCommands();
        this.registerListeners();
        this.setupMetrics();
        if (this.dataConverter.hasLocationsToConvert()) {
            this.dataConverter.convertLocationData();
        }
        this.refreezePlayers();

        new BukkitRunnable() {

            @Override
            public void run() {
                frozenPages.setupStrings();
            }
        }.runTaskLater(this, 20L);
    }

    @Override
    public void onDisable() {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (this.playerManager.isFrozen(p)) {
                FrozenPlayer frozenPlayer = this.playerManager.getFrozenPlayer(p);
                if (frozenPlayer.getOriginalLoc() != null) {
                    p.teleport(frozenPlayer.getOriginalLoc());
                }
                if (p.isFlying()) {
                    p.setFlying(false);
                    p.setAllowFlight(false);
                    p.teleport(this.locationManager.getGroundLocation(p.getLocation()));
                }
                p.getInventory().setHelmet(frozenPlayer.getHelmet());
                p.sendMessage(this.placeholders("{PREFIX}SimpleFreeze has been disabled, you will remain unfrozen until it is re-enabled"));
                if (frozenPlayer instanceof TempFrozenPlayer) {
                    ((TempFrozenPlayer) frozenPlayer).cancelTask();
                }
            }
        }
        this.movementManager.endTask();
        this.particleManager.endTask();
        this.mySQL.closeHikari();
    }

    public void setupHookBooleans() {
        PluginManager plManager = Bukkit.getServer().getPluginManager();
        this.usingLiteBans = plManager.getPlugin("LiteBans") != null;
        this.usingBanManager = plManager.getPlugin("BanManager") != null;
        this.usingEssentials = (plManager.getPlugin("Essentials") != null || plManager.getPlugin("EssentialsX") != null);
    }

    private void initializeVariables() {
        this.serverID = this.getConfig().getString("server-id");
        this.mySQL = new MySQL(this);
        this.playersConfig = new PlayersConfig(this);
        this.statsConfig = new StatsConfig(this);
        this.locationsConfig = new LocationsConfig(this);
        this.messageManager = new MessageManager(this);
        this.locationManager = new LocationManager(this);
        this.dataConverter = new DataConverter(this);
        this.soundManager = new SoundManager(this);
        this.frozenPages = new FrozenPages(this, this.locationManager);
        this.playerManager = new PlayerManager(this, this.frozenPages);
        this.particleManager = new ParticleManager(this, this.playerManager);
        this.movementManager = new MovementManager(this, this.playerManager);
        this.helmetManager = new HelmetManager(this, this.playerManager, this.locationManager);
        this.freezeManager = new FreezeManager(this, this.playerManager, this.helmetManager, this.locationManager, this.frozenPages, this.soundManager, this.messageManager);
        this.sqlManager = new SQLManager(this, this.mySQL, this.freezeManager, this.playerManager);
        this.setupHookBooleans();
    }

    private void loadConfigs() {
        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();
        this.reloadConfig();

        this.playersConfig.getConfig().options().copyDefaults(true);
        this.playersConfig.saveDefaultConfig();
        this.playersConfig.reloadConfig();

        this.statsConfig.getConfig().options().copyDefaults(true);
        this.statsConfig.saveDefaultConfig();
        this.statsConfig.reloadConfig();

        this.locationsConfig.getConfig().options().copyDefaults(true);
        this.locationsConfig.saveDefaultConfig();
        this.locationsConfig.reloadConfig();
    }

    private void registerCommands() {
        this.getCommand("simplefreeze").setExecutor(new SimpleFreezeCommand(this, this.helmetManager, this.frozenPages, this.particleManager, this.soundManager, this.messageManager, this.movementManager));
        this.getCommand("freeze").setExecutor(new FreezeCommand(this, this.playerManager, this.freezeManager, this.locationManager, this.sqlManager, this.permission));
        this.getCommand("tempfreeze").setExecutor(new TempFreezeCommand(this, this.playerManager, this.freezeManager, this.locationManager, this.sqlManager, this.permission));
        this.getCommand("unfreeze").setExecutor(new UnfreezeCommand(this, this.playerManager, this.freezeManager, this.sqlManager));
        this.getCommand("frozen").setExecutor(new FrozenCommand(this, this.frozenPages));
        this.getCommand("freezeall").setExecutor(new FreezeAllCommand(this, this.freezeManager, this.locationManager));
    }

    private void registerListeners() {
        PluginManager plManager = this.getServer().getPluginManager();
        plManager.registerEvents(new BlockBreakListener(this, this.playerManager), this);
        plManager.registerEvents(new BlockPlaceListener(this, this.playerManager), this);
        plManager.registerEvents(new EntityCombustListener(this, this.playerManager), this);
        plManager.registerEvents(new EntityDamageEntityListener(this, this.playerManager), this);
        plManager.registerEvents(new EntityDamageListener(this, this.playerManager), this);
        plManager.registerEvents(new InventoryClickListener(this, this.playerManager), this);
        plManager.registerEvents(new PlayerChatListener(this, this.playerManager), this);
        plManager.registerEvents(new PlayerCommandListener(this, this.playerManager), this);
        plManager.registerEvents(new PlayerDropListener(this, this.playerManager), this);
        plManager.registerEvents(new PlayerEditBookListener(this, this.playerManager), this);
        plManager.registerEvents(new PlayerTeleportListener(this, this.playerManager), this);
        plManager.registerEvents(new PlayerInteractListener(this, this.playerManager), this);
        plManager.registerEvents(new PlayerJoinListener(this, this.freezeManager, this.playerManager, this.locationManager, this.helmetManager, this.dataConverter, this.soundManager, this.messageManager), this);
        plManager.registerEvents(new PlayerQuitListener(this, this.playerManager, this.messageManager), this);
        plManager.registerEvents(new PlayerToggleFlightListener(this, this.playerManager), this);
        plManager.registerEvents(new ProjectileLaunchListener(this, this.playerManager), this);
        if (this.usingLiteBans()) {
            plManager.registerEvents(new LiteBansHook(this, this.playerManager, this.freezeManager), this);
        }
        if (this.usingBanManager()) {
            plManager.registerEvents(new BanManagerHook(this, this.playerManager, this.freezeManager), this);
        }
        if (this.usingEssentials()) {
            plManager.registerEvents(new EssentialsHook(this, this.playerManager, this.freezeManager), this);
        }
    }

    public void setupMetrics() {
        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.AdvancedPie("freeze_counts") {

            @Override
            public HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap) {
                valueMap.put("Freeze", statsConfig.getConfig().getInt("freeze-count"));
                valueMap.put("Temp Freeze", statsConfig.getConfig().getInt("temp-freeze-count"));
                valueMap.put("Freezeall", statsConfig.getConfig().getInt("freezeall-count"));
                valueMap.put("Unfreeze", statsConfig.getConfig().getInt("unfreeze-count"));
                return valueMap;
            }
        });
    }

    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public LocationManager getLocationManager() {
        return this.locationManager;
    }

    public MessageManager getMessageManager() {
        return this.messageManager;
    }

    public PlayersConfig getPlayerConfig() {
        return this.playersConfig;
    }

    public StatsConfig getStatsConfig() {
        return this.statsConfig;
    }

    public LocationsConfig getLocationsConfig() {
        return this.locationsConfig;
    }

    public SQLManager getSQLManager() {
        return this.sqlManager;
    }

    public String getServerID() {
        return this.serverID;
    }

    public boolean vaultEnabled() {
        return Bukkit.getPluginManager().getPlugin("Vault") != null;
    }

    public boolean usingLiteBans() {
        return this.usingLiteBans;
    }

    public boolean usingEssentials() {
        return this.usingEssentials;
    }

    public boolean usingBanManager() {
        return this.usingBanManager;
    }

    public boolean usingMySQL() {
        return this.usingMySQL;
    }


    public String placeholders(String arg) {
        return StringEscapeUtils.unescapeJava(ChatColor.translateAlternateColorCodes('&', arg.replace("{PREFIX}", this.getConfig().getString("prefix")).replace("{PREFIXFORMAT}", this.getFinalPrefixFormatting())));
    }

    public String getHelpMessage() {
        return this.placeholders("                                           &b&lSimpleFreeze\n" +
                "&b/sf &8- &7Displays this message\n" +
                "&b/sf reload &8- &7Reloads configuration file\n" +
                "&b/sf locations set <location-name> [placeholder] &8- &7Sets a location\n" +
                "&b/sf locations remove <location-name> &8- &7Removes a location\n" +
                "&b/frozenlist [page] &8- &7Lists frozen players\n" +
                "&b/freeze <name> [location/servers] [reason] &8- &7Freezes a player\n" +
                "&b/tempfreeze <name> <time> [location/servers] [reason] &8- &7Temporarily freezes a player\n" +
                "&b/unfreeze <name> &8- &7Unfreezes a player\n" +
                "&b/freezeall [reason] &8- &7Freeze all players\n");
    }

    public String getFinalPrefixFormatting() {
        return this.finalPrefixFormatting == null ? "" : this.finalPrefixFormatting;
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

    public void refreezePlayers() {
        for (final Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.hasPermission("sf.notify.update") && !UpdateNotifier.getLatestVersion().equals(UpdateNotifier.getCurrentVersion())) {
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        p.sendMessage(placeholders("{PREFIX}You are still running version &b" + UpdateNotifier.getCurrentVersion() + "\n{PREFIX}Latest version: &b" + UpdateNotifier.getLatestVersion()));

                    }

                }.runTaskLater(this, 25L);
            }

            final String uuidStr = p.getUniqueId().toString();
            FrozenPlayer frozenPlayer = null;

            if (DataConverter.hasDataToConvert(p)) {
                frozenPlayer = this.dataConverter.convertData(p);
                if (frozenPlayer != null) {
                    this.playerManager.addFrozenPlayer(p.getUniqueId(), frozenPlayer);
                }
            } else if (this.getPlayerConfig().getConfig().isSet("players." + uuidStr)) {
                Long freezeDate = this.getPlayerConfig().getConfig().getLong("players." + uuidStr + ".freeze-date");
                UUID freezerUUID = this.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freezer-uuid").equals("null") ? null : UUID.fromString(this.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freezer-uuid"));
                Location freezeLocation = SFLocation.fromString(this.getPlayerConfig().getConfig().getString("players." + uuidStr + ".freeze-location"));
                if (freezeLocation == null && this.getConfig().getBoolean("teleport-to-ground")) {
                    freezeLocation = new SFLocation(this.locationManager.getGroundLocation(p.getLocation()));
                } else if (freezeLocation == null) {
                    freezeLocation = new SFLocation(p.getLocation().clone());
                }
                Location originalLocation = SFLocation.fromString(this.getPlayerConfig().getConfig().getString("players." + uuidStr + ".original-location"));
                if (freezeLocation.equals(originalLocation)) {
                    freezeLocation = p.getLocation();
                    originalLocation = p.getLocation();
                } else {
                    originalLocation = p.getLocation();
                }
                String reason = this.getPlayerConfig().getConfig().getString("players." + uuidStr + ".reason");
                if (this.getPlayerConfig().getConfig().isSet("players." + uuidStr + ".unfreeze-date")) {
                    Long unfreezeDate = this.getPlayerConfig().getConfig().getLong("players." + uuidStr + ".unfreeze-date");
                    if (System.currentTimeMillis() < unfreezeDate) {
                        frozenPlayer = new TempFrozenPlayer(freezeDate, unfreezeDate, p.getUniqueId(), freezerUUID, originalLocation, freezeLocation, reason, this.playerManager.isSQLFrozen(p));
                        ((TempFrozenPlayer) frozenPlayer).startTask(this);
                        this.playerManager.addFrozenPlayer(p.getUniqueId(), frozenPlayer);
                    } else {
                        getPlayerConfig().getConfig().set("players." + uuidStr, null);
                        this.getPlayerConfig().saveConfig();
                        this.getPlayerConfig().reloadConfig();
                    }
                } else {
                    frozenPlayer = new FrozenPlayer(freezeDate, p.getUniqueId(), freezerUUID, originalLocation, freezeLocation, reason, this.playerManager.isSQLFrozen(p));
                    this.playerManager.addFrozenPlayer(p.getUniqueId(), frozenPlayer);
                }
            }

            if (frozenPlayer == null && this.freezeManager.freezeAllActive() && !(p.hasPermission("sf.exempt.*") || p.hasPermission("sf.exempt.freezeall"))) {
                UUID freezerUUID = this.getPlayerConfig().getConfig().getString("freezeall-info.freezer").equals("null") ? null : UUID.fromString(this.getPlayerConfig().getConfig().getString("freezeall-info.freezer"));

                SFLocation freezeLocation = this.getPlayerConfig().getConfig().getString("freezeall-info.location").equals("null") ? null : SFLocation.fromString(this.getPlayerConfig().getConfig().getString("freezeall-info.location"));
                if (freezeLocation == null && this.getPlayerConfig().getConfig().isSet("freezeall-info.players." + uuidStr + ".freeze-location")) {
                    freezeLocation = this.getPlayerConfig().getConfig().getString("freezeall-info.players." + uuidStr + ".freeze-location").equals("null") ? null : SFLocation.fromString(this.getPlayerConfig().getConfig().getString("freezeall-info.players." + uuidStr + ".freeze-location"));
                } else if (freezeLocation == null) {
                    if (this.getConfig().getBoolean("teleport-to-ground")) {
                        freezeLocation = new SFLocation(this.locationManager.getGroundLocation(p.getLocation()));
                    } else {
                        freezeLocation = new SFLocation(new SFLocation(p.getLocation().clone()));
                        if (this.getConfig().getBoolean("enable-fly")) {
                            p.setAllowFlight(true);
                            p.setFlying(true);
                        }
                    }
                }
                Location originalLocation = SFLocation.fromString(this.getPlayerConfig().getConfig().getString("freezeall-info.players." + uuidStr + ".original-location"));
                if (freezeLocation.equals(originalLocation)) {
                    freezeLocation = new SFLocation(p.getLocation());
                    originalLocation = p.getLocation();
                } else {
                    originalLocation = p.getLocation();
                }

                Long freezeDate = this.getPlayerConfig().getConfig().getLong("freezeall-info.date");
                String reason = this.getPlayerConfig().getConfig().getString("freezeall-info.reason");

                frozenPlayer = new FreezeAllPlayer(freezeDate, p.getUniqueId(), freezerUUID, originalLocation, freezeLocation, reason);
                this.playerManager.addFrozenPlayer(p.getUniqueId(), frozenPlayer);

                this.getPlayerConfig().getConfig().set("freezeall-info.players." + uuidStr + ".original-location", new SFLocation(frozenPlayer.getOriginalLoc()).toString());
                this.getPlayerConfig().getConfig().set("freezeall-info.players." + uuidStr + ".freeze-location", freezeLocation == null ? "null" : freezeLocation.toString());
                this.getPlayerConfig().saveConfig();
                this.getPlayerConfig().reloadConfig();

                final FrozenPlayer finalFreezeAllPlayer = frozenPlayer;

                new BukkitRunnable() {

                    @Override
                    public void run() {
                        finalFreezeAllPlayer.setHelmet(p.getInventory().getHelmet());
                        p.getInventory().setHelmet(helmetManager.getPersonalHelmetItem(finalFreezeAllPlayer));

                        if (finalFreezeAllPlayer.getFreezeLoc() == null) {
                            SFLocation originalLoc = new SFLocation(finalFreezeAllPlayer.getOriginalLoc());
                            Location freezeLoc;
                            if (getConfig().getBoolean("teleport-to-ground")) {
                                freezeLoc = locationManager.getGroundLocation(originalLoc);
                            } else {
                                freezeLoc = new SFLocation(originalLoc.clone());
                            }
                            finalFreezeAllPlayer.setFreezeLoc(freezeLoc);

                            if (getPlayerConfig().getConfig().getString("freezeall-info.players." + uuidStr + ".freeze-location").equals("null")) {
                                getPlayerConfig().getConfig().set("freezeall-info.players." + uuidStr + ".freeze-location", new SFLocation(freezeLoc).toString());
                            }
                        }

                        if (getConfig().getBoolean("enable-fly")) {
                            p.setAllowFlight(true);
                            p.setFlying(true);
                        }
                        p.teleport(finalFreezeAllPlayer.getFreezeLoc());

                        soundManager.playFreezeSound(p);

                        p.sendMessage(placeholders("{PREFIX}SimpleFreeze was re-enabled so you are now frozen again"));

                        String totalMsg = "";
                        if (!(finalFreezeAllPlayer.getFreezeLoc().equals(finalFreezeAllPlayer.getOriginalLoc()))) {
                            String locPlaceholder = locationManager.getLocationPlaceholder(locationManager.getLocationName(finalFreezeAllPlayer.getFreezeLoc()));
                            for (String msg : getConfig().getStringList("freezeall-location-message")) {
                                totalMsg += msg + "\n";
                            }
                            if (totalMsg.length() > 0) {
                                totalMsg = totalMsg.substring(0, totalMsg.length() - 2);
                            }
                            totalMsg = placeholders(totalMsg.replace("{LOCATION}", locPlaceholder).replace("{FREEZER}", finalFreezeAllPlayer.getFreezerName()).replace("{REASON}", reason));
                            p.sendMessage(totalMsg);
                        } else {
                            String locPlaceholder = getConfig().getString("location");
                            for (String msg : getConfig().getStringList("freezeall-message")) {
                                if (msg.equals("")) {
                                    msg = " ";
                                }
                                totalMsg += msg + "\n";
                            }
                            if (totalMsg.length() > 0) {
                                totalMsg = totalMsg.substring(0, totalMsg.length() - 2);
                            }
                            totalMsg = placeholders(totalMsg.replace("{LOCATION}", locPlaceholder).replace("{FREEZER}", finalFreezeAllPlayer.getFreezerName()).replace("{REASON}", reason));
                            p.sendMessage(totalMsg);
                        }

                        if (messageManager.getFreezeAllLocInterval() > 0) {
                            messageManager.addFreezeAllPlayer(p, totalMsg);
                        } else if (messageManager.getFreezeAllLocInterval() > 0) {
                            messageManager.addFreezeAllLocPlayer(p, totalMsg);
                        }
                    }
                }.runTaskLater(this, 10L);

            } else if (frozenPlayer != null) {
                final FrozenPlayer finalFrozenPlayer = frozenPlayer;
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        String reason = finalFrozenPlayer.getReason() == null ? finalFrozenPlayer.getReason() : getConfig().getString("default-reason");
                        finalFrozenPlayer.setHelmet(p.getInventory().getHelmet());
                        p.getInventory().setHelmet(helmetManager.getPersonalHelmetItem(finalFrozenPlayer));

                        if (getPlayerConfig().getConfig().getString("players." + uuidStr + ".original-location").equals("null")) {
                            finalFrozenPlayer.setOriginalLoc(p.getLocation());
                            getPlayerConfig().getConfig().set("players." + uuidStr + ".original-location", new SFLocation(p.getLocation()).toString());
                        }

                        if (finalFrozenPlayer.getFreezeLoc() == null) {
                            SFLocation originalLoc = new SFLocation(finalFrozenPlayer.getOriginalLoc());
                            Location freezeLoc;
                            if (getConfig().getBoolean("teleport-to-ground")) {
                                freezeLoc = locationManager.getGroundLocation(originalLoc);
                            } else {
                                freezeLoc = new SFLocation(originalLoc.clone());
                            }
                            finalFrozenPlayer.setFreezeLoc(freezeLoc);

                            if (getPlayerConfig().getConfig().getString("players." + uuidStr + ".freeze-location").equals("null")) {
                                getPlayerConfig().getConfig().set("players." + uuidStr + ".freeze-location", new SFLocation(freezeLoc).toString());
                            }
                        }

                        if (getConfig().getBoolean("enable-fly")) {
                            p.setAllowFlight(true);
                            p.setFlying(true);
                        }
                        p.teleport(finalFrozenPlayer.getFreezeLoc());

                        soundManager.playFreezeSound(p);

                        if (getPlayerConfig().getConfig().getBoolean("players. " + uuidStr + ".message", false)) {
                            String location = locationManager.getLocationName(finalFrozenPlayer.getFreezeLoc());
                            String freezerName = finalFrozenPlayer.getFreezerName();
                            String timePlaceholder = "";
                            String serversPlaceholder = "";
                            String locationPlaceholder = location == null ? getConfig().getString("location") : getConfig().getString("locations." + location + ".placeholder", location);
                            String path;
                            if (finalFrozenPlayer instanceof TempFrozenPlayer) {
                                timePlaceholder = TimeUtil.formatTime((((TempFrozenPlayer) finalFrozenPlayer).getUnfreezeDate() - System.currentTimeMillis()) / 1000L);
                                path = "first-join.temp-frozen";
                            } else {
                                path = "first-join.frozen";
                            }

                            if (location != null) {
                                path += "-location";
                            }
                            p.sendMessage(placeholders(getConfig().getString(path).replace("{PLAYER}", p.getName()).replace("{FREEZER}", freezerName).replace("{TIME}", timePlaceholder).replace("{LOCATION}", locationPlaceholder)));
                            getPlayerConfig().getConfig().set("players." + uuidStr + ".message", null);
                            getPlayerConfig().saveConfig();
                            getPlayerConfig().reloadConfig();
                        } else {
                            p.sendMessage(placeholders("{PREFIX}SimpleFreeze was re-enabled so you are now frozen again"));
                        }


                        String servers = getPlayerConfig().getConfig().getString("players." + uuidStr + ".servers", "");
                        String location = locationManager.getLocationName(finalFrozenPlayer.getFreezeLoc());
                        String locationPlaceholder = locationManager.getLocationPlaceholder(location);
                        String path = "";
                        if (finalFrozenPlayer instanceof TempFrozenPlayer) {
                            path = "temp-freeze-message";
                            if (location != null) {
                                path = "temp-freeze-location-message";
                            }
                        } else {
                            path = "freeze-message";
                            if (location != null) {
                                path = "freeze-location-message";
                            }
                        }

                        if (!servers.equals("")) {
                            path = "sql-" + path;
                        }

                        String msg = "";
                        for (String line : getConfig().getStringList(path)) {
                            if (line.equals("")) {
                                line = " ";
                            }
                            msg += line + "\n";
                        }
                        msg = msg.length() > 2 ? msg.substring(0, msg.length() - 1) : "";
                        msg = msg.replace("{PLAYER}", p.getName()).replace("{FREEZER}", finalFrozenPlayer.getFreezerName()).replace("{LOCATION}", locationPlaceholder).replace("{SERVERS}", servers).replace("{REASON}", reason);

                        if (finalFrozenPlayer instanceof TempFrozenPlayer) {
                            if (location == null && messageManager.getTempFreezeInterval() > 0) {
                                messageManager.addTempFreezePlayer(p, msg);
                            } else if (messageManager.getTempFreezeLocInterval() > 0) {
                                messageManager.addTempFreezeLocPlayer(p, msg);
                            }
                        } else {
                            if (location == null && messageManager.getFreezeInterval() > 0) {
                                messageManager.addFreezePlayer(p, msg);
                            } else if (messageManager.getFreezeLocInterval() > 0) {
                                messageManager.addFreezeLocPlayer(p, msg);
                            }
                        }
                    }
                }.runTaskLater(this, 10L);
            }
        }
    }

}
