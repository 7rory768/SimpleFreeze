package org.plugins.simplefreeze.managers;

import org.bukkit.entity.Player;
import org.plugins.simplefreeze.objects.FrozenPlayer;

import java.util.HashMap;
import java.util.UUID;

public class PlayerManager {

//	private final SimpleFreezeMain plugin;

    private HashMap<UUID, FrozenPlayer> frozenPlayers = new HashMap<UUID, FrozenPlayer>();

//	public PlayerManager(SimpleFreezeMain plugin) {
//		this.plugin = plugin;
//	}

    public HashMap<UUID, FrozenPlayer> getFrozenPlayers() {
        return this.frozenPlayers;
    }

    public void addFrozenPlayer(UUID uuid, FrozenPlayer frozenPlayer) {
        this.frozenPlayers.put(uuid, frozenPlayer);
    }

    public void removeFrozenPlayer(Player p) {
        this.frozenPlayers.remove(p.getUniqueId());
    }

    public void removeFrozenPlayer(UUID uuid) {
        this.frozenPlayers.remove(uuid);
    }

    public boolean isFrozen(Player p) {
        return this.isFrozen(p.getUniqueId());
    }

    public boolean isFrozen(UUID uuid) {
        return this.frozenPlayers.containsKey(uuid);
    }

    public boolean isSQLFrozen(UUID uuid) {
        return this.frozenPlayers.containsKey(uuid) ? this.frozenPlayers.get(uuid).isSqlFreeze() : false;
    }

    public FrozenPlayer getFrozenPlayer(Player p) {
        return this.getFrozenPlayer(p.getUniqueId());
    }

    public FrozenPlayer getFrozenPlayer(UUID uuid) {
        return this.isFrozen(uuid) ? this.frozenPlayers.get(uuid) : null;
    }

}
