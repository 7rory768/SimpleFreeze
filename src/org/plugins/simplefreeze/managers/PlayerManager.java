package org.plugins.simplefreeze.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.cache.FrozenPages;
import org.plugins.simplefreeze.objects.FrozenPlayer;

import java.util.HashMap;
import java.util.UUID;

public class PlayerManager {

	private final SimpleFreezeMain plugin;
	private final FrozenPages frozenPages;

    private HashMap<UUID, FrozenPlayer> frozenPlayers = new HashMap<UUID, FrozenPlayer>();

	public PlayerManager(SimpleFreezeMain plugin, FrozenPages frozenPages) {
		this.plugin = plugin;
		this.frozenPages = frozenPages;
	}

    public HashMap<UUID, FrozenPlayer> getFrozenPlayers() {
        return this.frozenPlayers;
    }

    public void addFrozenPlayer(UUID uuid, FrozenPlayer frozenPlayer) {
        this.frozenPlayers.put(uuid, frozenPlayer);
        this.frozenPages.refreshString(uuid);
        Bukkit.broadcastMessage("refreshing");
    }

    public void removeFrozenPlayer(Player p) {
        this.removeFrozenPlayer(p.getUniqueId());
    }

    public void removeFrozenPlayer(UUID uuid) {
        this.frozenPlayers.remove(uuid);
        this.frozenPages.removePlayer(uuid);
    }

    public boolean isFrozen(Player p) {
        return this.isFrozen(p.getUniqueId());
    }

    public boolean isFrozen(UUID uuid) {
       if (this.frozenPlayers.containsKey(uuid)) {
           return true;
       } else if (this.plugin.getPlayerConfig().getConfig().isSet("players." + uuid.toString())) {
           if (this.plugin.getPlayerConfig().getConfig().isSet("players." + uuid.toString() + ".unfreeze-date")) {
                if (System.currentTimeMillis() > this.plugin.getPlayerConfig().getConfig().getLong("players." + uuid.toString() + ".unfreeze-date")) {
                    return false;
                }
           }
           return true;
       }
       return false;
    }

    public boolean isSQLFrozen(UUID uuid) {
        return this.frozenPlayers.containsKey(uuid) ? this.frozenPlayers.get(uuid).isSqlFreeze() : this.plugin.getPlayerConfig().getConfig().getBoolean("players." + uuid.toString() + ".mysql", false);
    }

    public FrozenPlayer getFrozenPlayer(Player p) {
        return this.getFrozenPlayer(p.getUniqueId());
    }

    public FrozenPlayer getFrozenPlayer(UUID uuid) {
        return this.isFrozen(uuid) ? this.frozenPlayers.get(uuid) : null;
    }

}
