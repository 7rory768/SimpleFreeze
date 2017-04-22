package org.plugins.simplefreeze.hooks;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.managers.FreezeManager;
import org.plugins.simplefreeze.managers.PlayerManager;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EssentialsHook implements Listener {

    private final SimpleFreezeMain plugin;
    private final PlayerManager playerManager;
    private final FreezeManager freezeManager;

    public EssentialsHook(SimpleFreezeMain plugin, PlayerManager playerManager, FreezeManager freezeManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.freezeManager = freezeManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBan(PlayerCommandPreprocessEvent e) {
        if (this.plugin.usingEssentials() && !e.isCancelled()) {
            String cmd = e.getMessage();
            String lowerCaseCmd = e.getMessage().toLowerCase();
            if (lowerCaseCmd.startsWith("/ban") || lowerCaseCmd.startsWith("/tempban") || lowerCaseCmd.startsWith("/banip")) {
                String[] args = cmd.split("\\s+");
                if (args.length > 1) {
                    final String arg2 = args[1];

                    UUID uuid = null;
                    Player p = Bukkit.getPlayer(arg2);
                    OfflinePlayer offlineP = Bukkit.getOfflinePlayer(arg2);

                    if (p != null) {
                        uuid = p.getUniqueId();
                    } else if (offlineP != null) {
                        if (offlineP.hasPlayedBefore()) {
                            uuid = offlineP.getUniqueId();
                        }
                    }

                    if (uuid != null) {
                        if (args[0].equalsIgnoreCase("/ban") || args[0].equalsIgnoreCase("/tempban")) {
                            final UUID finalUUID = uuid;
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (playerManager.isFrozen(finalUUID) && !playerManager.isFreezeAllFrozen(finalUUID) && isBanned(finalUUID)) {
                                        freezeManager.unfreeze(finalUUID);
                                    }
                                }
                            }.runTaskLater(this.plugin, 1L);
                        }
                    }

                    if (args[0].equalsIgnoreCase("/banip")) {
                        List<UUID> potentionalPlayers = new ArrayList<>();
                        for (Player p1 : Bukkit.getOnlinePlayers()) {
                            if (p1.getAddress().getHostName().equals(arg2)) {
                                potentionalPlayers.add(p1.getUniqueId());
                            }
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (isBanned(arg2)) {
                                    for (UUID uuid : potentionalPlayers) {
                                        if (playerManager.isFrozen(uuid) && !playerManager.isFreezeAllFrozen(uuid)) {
                                            freezeManager.unfreeze(uuid);
                                        }
                                    }
                                }


                            }
                        }.runTaskLater(this.plugin, 1L);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (playerManager.isFrozen(uuid) && !playerManager.isFreezeAllFrozen(uuid) && isBanned(e.getPlayer().getAddress().getHostName())) {
                    freezeManager.unfreeze(uuid);
                }
            }
        }.runTaskLater(this.plugin, 1L);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (playerManager.isFrozen(uuid) && !playerManager.isFreezeAllFrozen(uuid) && isBanned(e.getPlayer().getAddress().getHostName())) {
                    freezeManager.unfreeze(uuid);
                }
            }
        }.runTaskLater(this.plugin, 1L);
    }

    private boolean isBanned(UUID uuid) {
        JSONParser parser = new JSONParser();
        String uuidStr = uuid.toString();
        try {
            Object parsedObj = parser.parse(new FileReader("banned-players.json"));
            JSONArray list = (JSONArray) parsedObj;
            for (Object obj : list) {
                JSONObject jsonObj = (JSONObject) obj;
                if (((String) jsonObj.get("uuid")).equalsIgnoreCase(uuidStr)) {
                    return true;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        parser.reset();
        return false;
    }

    private boolean isBanned(String ip) {
        JSONParser parser = new JSONParser();
        try {
            Object parsedObj = parser.parse(new FileReader("banned-ips.json"));
            JSONArray list = (JSONArray) parsedObj;
            for (Object obj : list) {
                JSONObject jsonObj = (JSONObject) obj;
                if (((String) jsonObj.get("ip")).equalsIgnoreCase(ip)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        parser.reset();
        return false;
    }

}