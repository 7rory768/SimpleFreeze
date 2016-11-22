package org.plugins.simplefreeze.managers;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.plugins.simplefreeze.objects.FrozenPlayer;

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
	
	public void removeFrozenPlayer(UUID uuid) {
		this.frozenPlayers.remove(uuid);
	}
	
	public boolean isFrozen(Player p) {
		return this.frozenPlayers.containsKey(p.getUniqueId());
	}
	
	public boolean isFrozen(UUID uuid) {
		return this.frozenPlayers.containsKey(uuid);
	}
	
	public boolean isSQLFrozen(UUID uuid) {
		return this.frozenPlayers.containsKey(uuid) ? this.frozenPlayers.get(uuid).isSqlFreeze() : false;
	}
	
	public FrozenPlayer getFrozenPlayer(UUID uuid) {
		return this.isFrozen(uuid) ? this.frozenPlayers.get(uuid) : null;
	}
	
}
