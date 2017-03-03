package org.plugins.simplefreeze.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.plugins.simplefreeze.SimpleFreezeMain;
import org.plugins.simplefreeze.cache.FrozenPages;
import org.plugins.simplefreeze.objects.FreezeAllPlayer;
import org.plugins.simplefreeze.objects.FrozenPlayer;
import org.plugins.simplefreeze.objects.SFLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class PlayerManager {

    private final SimpleFreezeMain plugin;
    private final FrozenPages frozenPages;

    private HashMap<UUID, FrozenPlayer> frozenPlayers = new HashMap<UUID, FrozenPlayer>();
    private HashSet<UUID> fallingPlayers = new HashSet<>();

    public PlayerManager(SimpleFreezeMain plugin, FrozenPages frozenPages) {
        this.plugin = plugin;
        this.frozenPages = frozenPages;
    }

    public HashMap<UUID, FrozenPlayer> getFrozenPlayers() {
        return this.frozenPlayers;
    }

    public void addFrozenPlayer(UUID uuid, FrozenPlayer frozenPlayer) {
        this.frozenPlayers.put(uuid, frozenPlayer);
        if (!(frozenPlayer instanceof FreezeAllPlayer)) {
            this.frozenPages.refreshString(uuid);
        }
    }

    public void removeFrozenPlayer(Player p) {
        this.removeFrozenPlayer(p.getUniqueId());
    }

    public void removeFrozenPlayer(UUID uuid) {
        if (this.frozenPlayers.containsKey(uuid)) {
            if (!(this.frozenPlayers.get(uuid) instanceof FreezeAllPlayer)) {
                this.frozenPages.removePlayer(uuid);
            }
            this.frozenPlayers.remove(uuid);
        }
    }

    public boolean isFrozen(Player p) {
        return this.isFrozen(p.getUniqueId());
    }

    public boolean isFrozen(UUID uuid) {
        if (this.frozenPlayers.containsKey(uuid)) {
            return true;
        } else if (this.plugin.getPlayerConfig().getConfig().isSet("players." + uuid.toString())) {
            if (this.plugin.getPlayerConfig().getConfig().isSet("players." + uuid.toString() + ".unfreeze-date")) {
                if (System.currentTimeMillis() >= this.plugin.getPlayerConfig().getConfig().getLong("players." + uuid.toString() + ".unfreeze-date")) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isFreezeAllFrozen(UUID uuid) {
        if (this.frozenPlayers.containsKey(uuid)) {
            if (this.frozenPlayers.get(uuid) instanceof FreezeAllPlayer) {
                return true;
            }
        } else if (this.plugin.getPlayerConfig().getConfig().isSet("freezeall-info.players." + uuid.toString())) {
            return true;
        }
        return false;
    }

    public boolean isSQLFrozen(UUID uuid) {
        return this.frozenPlayers.containsKey(uuid) ? this.frozenPlayers.get(uuid).isSqlFreeze() : this.plugin.getPlayerConfig().getConfig().getBoolean("players." + uuid.toString() + ".mysql", false);
    }

    public Location getOriginalLocation(UUID uuid) {
        if (this.isFrozen(uuid)) {
            if (this.frozenPlayers.containsKey(uuid)) {
                return this.frozenPlayers.get(uuid).getOriginalLoc();
            } else if (this.plugin.getPlayerConfig().getConfig().isSet("players." + uuid.toString())) {
                return SFLocation.fromString(this.plugin.getPlayerConfig().getConfig().getString("players." + uuid.toString() + ".original-location"));
            }
        }
        return null;
    }

    public FrozenPlayer getFrozenPlayer(Player p) {
        return this.getFrozenPlayer(p.getUniqueId());
    }

    public FrozenPlayer getFrozenPlayer(UUID uuid) {
        return this.isFrozen(uuid) ? this.frozenPlayers.get(uuid) : null;
    }

    public boolean isFallingPlayer(UUID uuid) {
        return this.fallingPlayers.contains(uuid) || this.plugin.getPlayerConfig().getConfig().getStringList("falling-players").contains(uuid.toString());
    }

    public void addFallingPlayer(UUID uuid) {
        this.fallingPlayers.add(uuid);
    }

    public void removeFallingPlayer(UUID uuid) {
        this.fallingPlayers.remove(uuid);
        List<String> fallingList = this.plugin.getPlayerConfig().getConfig().getStringList("falling-players");
        if (fallingList.contains(uuid.toString())) {
            fallingList.remove(uuid.toString());

        }
    }

}
